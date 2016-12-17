package com.github.nizshee.server;


import com.github.nizshee.Message;
import com.github.nizshee.shared.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;


@SuppressWarnings("all")
public class TcpAsyncServer extends Server {

    private final Object isRunning = new Object();

    public TcpAsyncServer(Message.Start start) throws IOException {
        super(start);
    }

    @Override
    public Message.ServerResult start() throws IOException {
        AsynchronousServerSocketChannel socket = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(host, port));
        socket.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Void attachment) {
                socket.accept(null, this);
                new AsyncRunnable(result, requestCount).run();
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
            }
        });

        try {
            synchronized (isRunning) {
                isRunning.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        socket.close();

        return result();
    }

    @Override
    public void stop() throws IOException {
        synchronized (isRunning) {
            isRunning.notifyAll();
        }
    }

    private class AsyncRunnable implements Runnable {

        private final AsynchronousSocketChannel socket;
        private final int timesLeft;

        public AsyncRunnable(AsynchronousSocketChannel socket, int timesLeft) {
            this.socket = socket;
            this.timesLeft = timesLeft;
        }

        @Override
        public void run() {
            if (timesLeft < 1) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            long start = System.currentTimeMillis();
            ByteBuffer buffer = ByteBuffer.allocate(4);

            socket.read(buffer, null, new CompletionHandler<Integer, Void>() {
                @Override
                public void completed(Integer result, Void attachment) {
                    if (buffer.position() != buffer.capacity()) {
                        socket.read(buffer, null, this);
                        return;
                    }

                    buffer.flip();
                    int size = buffer.getInt();
                    ByteBuffer buffer1 = ByteBuffer.allocate(size);

                    socket.read(buffer1, null, new CompletionHandler<Integer, Void>() {
                        @Override
                        public void completed(Integer result, Void attachment) {
                            if (buffer1.position() != buffer1.capacity()) {
                                socket.read(buffer1, null, this);
                                return;
                            }
                            buffer1.flip();
                            try {
                                Message.Arr arr = Message.Arr.parseFrom(buffer1.array());
                                Integer[] array = arr.getValuesList().toArray(new Integer[arr.getValuesCount()]);
                                handleSort(array);
                                byte[] bytes = Util.writeArray(array);
                                socket.write(ByteBuffer.wrap(bytes)); // TODO
                                long end = System.currentTimeMillis();
                                updateClient(end - start);

                                new AsyncRunnable(socket, timesLeft - 1).run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failed(Throwable exc, Void attachment) {
                          exc.printStackTrace();
                        }
                    });
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    exc.printStackTrace();
                }
            });


        }
    }
}
