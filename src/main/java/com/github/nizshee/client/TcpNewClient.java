package com.github.nizshee.client;


import com.github.nizshee.Message;
import com.github.nizshee.shared.Util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

@SuppressWarnings("all")
public class TcpNewClient extends Client {

    public TcpNewClient(Message.Start start) throws IOException {
        super(start);
    }

    @Override
    public Message.ClientResult start() throws IOException {
        Thread[] threads = new Thread[clientCount];
        Integer[] array = generateArray();
        for (int i = 0; i < clientCount; ++i) {
            threads[i] = new Thread(new RunnableNew(array));
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

    private class RunnableNew implements Runnable {

        private final Integer[] array;

        public RunnableNew(Integer[] array) {
            this.array = array;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            for (int i = 0; i < requestCount; ++i) {
                try (Socket socket = new Socket(host, port)) {
                    Util.writeArray(socket.getOutputStream(), array);
                    Util.readArray(socket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(delta);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long end = System.currentTimeMillis();
            update(end - start);
        }
    }
}
