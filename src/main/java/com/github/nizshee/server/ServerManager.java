package com.github.nizshee.server;


import com.github.nizshee.Message;
import com.github.nizshee.shared.Util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("all")
public class ServerManager {

    public static void main(String[] args) throws Exception {
        InetAddress ia = InetAddress.getByName(args.length > 0 ? args[0]: "localhost");
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 12345;

        ServerSocket serverSocket = new ServerSocket(port, 50, ia);
        while (true) {
            try (Socket socket = serverSocket.accept()) {
                System.out.println("Session started.");
                byte[] buffer = Util.read(socket.getInputStream());
                Message.Start start = Message.Start.parseFrom(buffer);

                Server server = selectArch(start);

                ServerRunnable runnable = new ServerRunnable(server);
                Thread thread = new Thread(runnable);
                thread.start();
                Util.readOk(socket.getInputStream());
                server.stop();
                thread.join();
                Message.ServerResult result = runnable.getResult();
                Util.write(socket.getOutputStream(), result.toByteArray());

                System.out.println("Session ended.");
            }
        }
    }

    private static Server selectArch(Message.Start start) throws Exception {
        switch (start.getArch()) {
            case TcpContThread:
                return new TcpContThreadServer(start);
            case TcpContPool:
                return new TcpContPoolServer(start);
            case TcpContNonblock:
                return new TcpContNonblockServer(start);
            case TcpNew:
                return new TcpNewServer(start);
            case UdpThread:
                return new UdpThreadServer(start);
            case UdpPool:
                return new UdpPoolServer(start);
            case TcpAssync:
                return new TcpAsyncServer(start);
        }

        throw new Exception();
    }

    private static class ServerRunnable implements Runnable {
        private final Server server;
        private volatile IOException exception;
        private volatile Message.ServerResult result;

        public ServerRunnable(Server server) {
            this.server = server;
        }

        @Override
        public void run() {
            try {
                result = server.start();
            } catch (IOException e) {
                exception = e;
            }
        }

        Message.ServerResult getResult() throws IOException {
            if (exception != null) throw exception;
            return result;
        }
    }
}
