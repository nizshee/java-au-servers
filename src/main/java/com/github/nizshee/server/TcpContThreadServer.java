package com.github.nizshee.server;


import com.github.nizshee.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


@SuppressWarnings("all")
public class TcpContThreadServer extends Server {

    public TcpContThreadServer(Message.Start start) throws IOException {
        super(start);
    }

    @Override
    public Message.ServerResult start() throws IOException {
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
                new Thread(runnable).start();
            }
        } catch (SocketException ignore) {
        }

        return result();
    }
}
