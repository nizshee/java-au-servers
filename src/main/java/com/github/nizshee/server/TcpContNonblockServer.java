package com.github.nizshee.server;


import com.github.nizshee.Message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;


@SuppressWarnings("all")
public class TcpContNonblockServer extends Server {

    public TcpContNonblockServer(Message.Start start) throws IOException {
        super(start);
    }

    @Override
    public Message.ServerResult start() throws IOException {
        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
            serverSocket.bind(new InetSocketAddress(host, port));
            serverSocket.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            while (!isReady()) {
                selector.selectNow();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if (key.isAcceptable()) {
                        SocketChannel socket = serverSocket.accept();
                        if (socket != null) {
                            socket.configureBlocking(false);
                            socket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new ConnectionContext());
                        }
                    }
                    if (key.isReadable()) {
                        ConnectionContext context = (ConnectionContext) key.attachment();
                        context.read((SocketChannel) key.channel());
                        Optional<Integer[]> optional = context.getMessage();
                        if (optional.isPresent()) {
                            Integer[] array = optional.get();
                            handleSort(array);
                            context.write(array);
                        }
                    }

                    if (key.isWritable()) {
                        ConnectionContext context = (ConnectionContext) key.attachment();
                        context.write((SocketChannel) key.channel());
                        if (context.getCounter() == requestCount) {
                            key.channel().close();
                        }
                    }

                    iterator.remove();
                }
            }
        }
        return result();
    }

    private final class ConnectionContext {
        private long start;
        private ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
        private ByteBuffer buffer = null;
        private ByteBuffer toWrite = null;
        private int size = -1;
        private int counter = 0;

        public int getCounter() {
            return counter;
        }


        public void write(Integer[] array) throws IOException {
            if (toWrite != null) throw new IOException("Too many messages to write");
            Message.Arr arr = Message.Arr.newBuilder().addAllValues(Arrays.asList(array)).build();
            int size = arr.getSerializedSize();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(size + 4);
            new DataOutputStream(bos).writeInt(size);
            bos.write(arr.toByteArray());
            toWrite = ByteBuffer.wrap(bos.toByteArray());
        }

        public void write(SocketChannel socket) throws IOException {
            if (toWrite == null) return;
            socket.write(toWrite);
            if (toWrite.position() == toWrite.capacity()) {
                long end = System.currentTimeMillis();
                updateClient(end - start);
                counter++;
                toWrite = null;
            }
        }

        public void read(SocketChannel socket) throws IOException {
            if (buffer == null) {
                socket.read(sizeBuffer);
            } else {
                socket.read(buffer);
            }
        }

        public Optional<Integer[]> getMessage() throws IOException {
            if (sizeBuffer.position() == sizeBuffer.capacity()) {
                start = System.currentTimeMillis();
                sizeBuffer.flip();
                size = sizeBuffer.getInt();
                sizeBuffer.flip();
                sizeBuffer.clear();
            }
            if (size != -1 && buffer == null) {
                buffer = ByteBuffer.allocate(size);
            }
            if (buffer != null && buffer.position() == buffer.capacity()) {
                buffer.flip();
                Message.Arr arr = Message.Arr.parseFrom(buffer.array());
                buffer = null;
                size = -1;
                return Optional.of(arr.getValuesList().toArray(new Integer[arr.getValuesCount()]));
            }
            return Optional.empty();
        }
    }
}
