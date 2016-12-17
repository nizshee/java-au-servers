package com.github.nizshee.server;


import com.github.nizshee.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


@SuppressWarnings("all")
public class TcpNewServer extends Server {

    public TcpNewServer(Message.Start start) throws IOException {
        super(start);
    }

    @Override
    public Message.ServerResult start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port, 500, host)) {
            while (!isReady()) {
                try (Socket socket = serverSocket.accept()) {
                    handleRequest(socket);
                }
            }
        }
        return result();
    }
}
