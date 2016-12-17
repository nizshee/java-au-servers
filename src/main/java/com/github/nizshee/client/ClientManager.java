package com.github.nizshee.client;


import com.github.nizshee.Message;
import com.github.nizshee.shared.Util;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientManager {

    public static void main(String[] args) throws Exception {
        InetAddress ia = InetAddress.getByName(args.length > 0 ? args[0]: "localhost");
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 12346;

        ServerSocket serverSocket = new ServerSocket(port, 50, ia);
        //noinspection InfiniteLoopStatement
        while (true) {
            try (Socket socket = serverSocket.accept()) {
                System.out.println("Session started.");
                byte[] buffer = Util.read(socket.getInputStream());
                Message.Start start = Message.Start.parseFrom(buffer);

                Client client = selectArch(start);
                Thread.sleep(1000);
                client.start();
                Message.ClientResult result = client.result();
                Util.write(socket.getOutputStream(), result.toByteArray());

                System.out.println("Session ended.");
            }
        }
    }

    private static Client selectArch(Message.Start start) throws Exception {
        switch (start.getArch()) {
            case TcpContThread:
                return new TcpContClient(start);
            case TcpContPool:
                return new TcpContClient(start);
            case TcpContNonblock:
                return new TcpContClient(start);
            case TcpNew:
                return new TcpNewClient(start);
            case UdpThread:
                return new UdpClient(start);
            case UdpPool:
                return new UdpClient(start);
            case TcpAssync:
                return new TcpContClient(start);
        }

        throw new Exception();
    }
}
