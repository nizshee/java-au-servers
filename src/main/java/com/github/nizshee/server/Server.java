package com.github.nizshee.server;


import com.github.nizshee.Message;
import com.github.nizshee.shared.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


@SuppressWarnings("all")
public abstract class Server {

    protected final int clientCount;
    protected final int requestCount;
    protected final InetAddress host;
    protected final int port;
    protected final int size;

    private final AtomicInteger timesRequest = new AtomicInteger(0);
    private final AtomicLong totalRequest = new AtomicLong(0);

    private final AtomicInteger timesClient = new AtomicInteger(0);
    private final AtomicLong totalClient = new AtomicLong(0);

    public Server(Message.Start start) throws IOException {
        clientCount = start.getClientCount();
        requestCount = start.getRequestCount();
        host = InetAddress.getByName(start.getServer());
        port = start.getPort();
        size = start.getArraySize();
    }

    abstract public Message.ServerResult start() throws IOException;
    public void stop() throws IOException {
    }

    protected void handleSort(Integer[] array) {
        long start = System.currentTimeMillis();
        sort(array);
        long end = System.currentTimeMillis();

        totalRequest.addAndGet(end - start);
        timesRequest.incrementAndGet();
    }

    protected boolean isReady() {
        return timesRequest.get() >= (clientCount * requestCount);
    }

    protected void updateClient(long delta) {
        totalClient.addAndGet(delta);
        timesClient.incrementAndGet();
    }

    protected Message.ServerResult result() {
        return Message.ServerResult.newBuilder()
                .setClient((int) (totalClient.get() / timesClient.get()))
                .setRequest((int) (totalRequest.get() / timesRequest.get()))
                .build();
    }

    protected void handleRequest(Socket socket) throws IOException{
        long start = System.currentTimeMillis();
        Integer[] array = Util.readArray(socket.getInputStream());
        handleSort(array);
        Util.writeArray(socket.getOutputStream(), array);
        long end = System.currentTimeMillis();
        updateClient(end - start);
    }

    private static void sort(Integer[] array) {
        Integer tmp;
        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = 1; i < array.length; ++i) {
                if (array[i - 1] > array[i]) {
                    tmp = array[i - 1];
                    array[i - 1] = array[i];
                    array[i] = tmp;
                    flag = true;
                }
            }
        }
    }


    protected class ContRunnable implements Runnable {
        private final Socket socket;
        private final Runnable onClose;

        public ContRunnable(Socket socket, Runnable onClose) {
            this.socket = socket;
            this.onClose = onClose;
        }

        @Override
        public void run()  {
            try {
                for (int i = 0; i < requestCount; ++i) {
                    handleRequest(socket);
                }
                if (isReady()) {
                    onClose.run();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected class UdpRunnable implements Runnable {

        private final DatagramPacket packet;

        public UdpRunnable(DatagramPacket packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            try {
                long start = System.currentTimeMillis();
                Integer[] array = Util.readArray(packet.getData());
                handleSort(array);
                byte[] bytes = Message.Arr.newBuilder().addAllValues(Arrays.asList(array)).build().toByteArray();
                DatagramPacket packet1 = new DatagramPacket(bytes, bytes.length, packet.getAddress(), packet.getPort());
                new DatagramSocket().send(packet1);
                long end = System.currentTimeMillis();
                updateClient(end - start);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
