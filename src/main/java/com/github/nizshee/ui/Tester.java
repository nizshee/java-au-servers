package com.github.nizshee.ui;


import com.github.nizshee.Message;
import com.github.nizshee.shared.Util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Tester {

    private final InetAddress serverHost;
    private final InetAddress clientHost;
    private final int serverPort;
    private final int clientPort;

    public Tester(InetAddress serverHost, InetAddress clientHost, int serverPort, int clientPort) {
        this.serverHost = serverHost;
        this.clientHost = clientHost;
        this.serverPort = serverPort;
        this.clientPort = clientPort;
    }

    public List<Result> start(int left, int right, int step, Parameter parameter, Message.Arch arch, int requestCount,
                              int clientCount, int delta, int size, String server, int port) throws IOException {

        switch (arch) {

            case TcpContThread:
                System.out.print("TcpContThread");
                break;
            case TcpContPool:
                System.out.print("TcpContPool");
                break;
            case TcpContNonblock:
                System.out.print("TcpContNonblock");
                break;
            case TcpNew:
                System.out.print("TcpNew");
                break;
            case UdpThread:
                System.out.print("UdpThread");
                break;
            case UdpPool:
                System.out.print("UdpPool");
                break;
            case TcpAssync:
                System.out.print("TcpAssync");
                break;
        }

        System.out.print(" ");

        switch (parameter) {
            case DELTA:
                System.out.print("delta");
                break;
            case SIZE:
                System.out.print("size");
                break;
            case COUNT:
                System.out.print("count");
                break;
        }

        System.out.println();


        List<Result> list = new LinkedList<>();
        Message.Start start = null;
        for (int i = left; i <= right; i += step) {
            switch (parameter) {
                case DELTA:
                    start = Message.Start.newBuilder()
                            .setClientCount(clientCount)
                            .setRequestCount(requestCount)
                            .setDelta(i)
                            .setArraySize(size)
                            .setArch(arch)
                            .setServer(server)
                            .setPort(port)
                            .build();
                    break;
                case SIZE:
                    start = Message.Start.newBuilder()
                            .setClientCount(clientCount)
                            .setRequestCount(requestCount)
                            .setDelta(delta)
                            .setArraySize(i)
                            .setArch(arch)
                            .setServer(server)
                            .setPort(port)
                            .build();
                    break;
                case COUNT:
                    start = Message.Start.newBuilder()
                            .setClientCount(i)
                            .setRequestCount(requestCount)
                            .setDelta(delta)
                            .setArraySize(size)
                            .setArch(arch)
                            .setServer(server)
                            .setPort(port)
                            .build();
                    break;
            }
            list.add(getResult(start));
            System.out.println("parameter = " + i);
        }
        return list;
    }

    @SuppressWarnings("all")
    public Result getResult(Message.Start start) throws IOException {
        Message.ClientResult clientResult;
        Message.ServerResult serverResult;
        byte[] buffer;
        try (Socket serverManager = new Socket(serverHost, serverPort)) {
            Util.write(serverManager.getOutputStream(), start.toByteArray());
            try (Socket clientManager = new Socket(clientHost, clientPort)) {
                Util.write(clientManager.getOutputStream(), start.toByteArray());
                buffer = Util.read(clientManager.getInputStream());
                clientResult = Message.ClientResult.parseFrom(buffer);
            }
            Util.writeOk(serverManager.getOutputStream());
            buffer = Util.read(serverManager.getInputStream());
            serverResult = Message.ServerResult.parseFrom(buffer);
        }

        return new Result(serverResult.getClient(), serverResult.getRequest(), clientResult.getClient());
    }

    public enum Parameter {
        DELTA,
        SIZE,
        COUNT
    }
}
