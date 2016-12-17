package com.github.nizshee.server;


import com.github.nizshee.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@SuppressWarnings("all")
public class TcpContPoolServer extends Server {

    public TcpContPoolServer(Message.Start start) throws IOException {
        super(start);
    }

    @Override
    public Message.ServerResult start() throws IOException {
        ExecutorService executor = Executors.newCachedThreadPool();
        try (ServerSocket server = new ServerSocket(port, 50, host)) {
            while (true) {
                Socket socket = server.accept();
                Runnable runnable = new ContRunnable(socket, () -> {
                    try {
                        server.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                executor.submit(runnable);
            }
        } catch (SocketException ignore) {
        }

        return result();
    }
}
