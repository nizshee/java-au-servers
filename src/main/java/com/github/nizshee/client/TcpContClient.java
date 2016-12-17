package com.github.nizshee.client;


import com.github.nizshee.Message;
import com.github.nizshee.shared.Util;

import java.io.IOException;
import java.net.Socket;

@SuppressWarnings("all")
public class TcpContClient extends Client {

    public TcpContClient(Message.Start start) throws IOException {
        super(start);
    }

    @Override
    public Message.ClientResult start() throws IOException {
        Thread[] threads = new Thread[clientCount];
        Integer[] array = generateArray();
        for (int i = 0; i < clientCount; ++i) {
            Socket socket = new Socket(host, port);
            threads[i] = new Thread(new ContRunnable(socket, array));
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

    private class ContRunnable implements Runnable {

        private final Socket server;
        private final Integer[] array;

        public ContRunnable(Socket server, Integer[] array) {
            this.server = server;
            this.array = array;
        }

        @Override
        public void run() {
            try (Socket socket = server) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < requestCount; ++i) {
                    Util.writeArray(socket.getOutputStream(), array);
                    Util.readArray(socket.getInputStream());

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
