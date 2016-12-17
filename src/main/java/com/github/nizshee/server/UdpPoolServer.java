package com.github.nizshee.server;


import com.github.nizshee.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@SuppressWarnings("all")
public class UdpPoolServer extends Server {

    private volatile DatagramSocket datagramSocket;

    public UdpPoolServer(Message.Start start) throws IOException {
        super(start);
    }

    @Override
    public Message.ServerResult start() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try (DatagramSocket socket = new DatagramSocket(port, host)) {
            datagramSocket = socket;
            socket.setSoTimeout(50000);
            while (true) {
                byte[] buffer = new byte[8 * size];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                executor.submit(new UdpRunnable(packet));
            }
        } catch (SocketException e) {
            if (!e.getMessage().equals("Socket closed")) throw e;
        }

        return result();
    }

    @Override
    public void stop() throws IOException {
        datagramSocket.close();
    }
}
