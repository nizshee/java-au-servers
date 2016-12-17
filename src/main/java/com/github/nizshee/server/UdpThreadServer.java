package com.github.nizshee.server;


import com.github.nizshee.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


@SuppressWarnings("all")
public class UdpThreadServer extends Server {

    private volatile DatagramSocket datagramSocket;

    public UdpThreadServer(Message.Start start) throws IOException {
        super(start);
    }

    @Override
    public Message.ServerResult start() throws IOException {
        try (DatagramSocket socket = new DatagramSocket(port, host)) {
            datagramSocket = socket;
            socket.setSoTimeout(50000);
            while (true) {
                byte[] buffer = new byte[8 * size];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                new Thread(new UdpRunnable(packet)).start();
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
