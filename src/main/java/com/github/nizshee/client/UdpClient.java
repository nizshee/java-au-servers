package com.github.nizshee.client;


import com.github.nizshee.Message;
import com.github.nizshee.shared.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;


@SuppressWarnings("all")
public class UdpClient extends Client {

    public UdpClient(Message.Start start) throws IOException {
        super(start);
    }

    @Override
    public Message.ClientResult start() throws IOException {
        Thread[] threads = new Thread[clientCount];
        Integer[] array = generateArray();
        for (int i = 0; i < clientCount; ++i) {
            threads[i] = new Thread(new UdpRunnable(array));
            threads[i].start();
        }

        for (int i = 0; i < clientCount; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result();
    }

    private class UdpRunnable implements Runnable {

        private final Integer[] array;

        public UdpRunnable(Integer[] array) {
            this.array = array;
        }

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(200);
                int count = 0;
                long start = System.currentTimeMillis();
                for (int i = 0; i < requestCount; ++i) {

                    byte[] bytes = Util.writeArray(array);
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, host, port);
                    socket.send(packet);
                    byte[] buffer = new byte[8 * size];
                    DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length);
                    try {
                        socket.receive(packet1);
                    } catch (SocketTimeoutException ignore) {
                        --i; // resend
                        continue;
                    }

                    ++count;

                    try {
                        Thread.sleep(delta);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                long end = System.currentTimeMillis();
                update(end - start);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
