package com.github.nizshee.client;


import com.github.nizshee.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("all")
public abstract class Client {

    protected final int clientCount;
    protected final int requestCount;
    protected final int delta;
    protected final int size;
    protected final InetAddress host;
    protected final int port;

    private final AtomicLong total = new AtomicLong(0);
    private final AtomicLong times = new AtomicLong(0);

    public Client(Message.Start start) throws IOException {
        this.clientCount = start.getClientCount();
        this.requestCount = start.getRequestCount();
        this.delta = start.getDelta();
        this.size = start.getArraySize();
        this.host = InetAddress.getByName(start.getServer());
        this.port = start.getPort();
    }

    abstract public Message.ClientResult start() throws IOException;

    protected void update(long delta) {
        total.addAndGet(delta);
        times.incrementAndGet();
    }

    protected Message.ClientResult result() {
        return Message.ClientResult.newBuilder()
                .setClient((int) (total.get() / times.get()))
                .build();
    }

    public Integer[] generateArray() {
        Random r = new Random(239);
        Integer[] array = new Integer[size];
        for (int i = 0; i < size; ++i) {
            array[i] = r.nextInt();
        }
        return array;
    }
}
