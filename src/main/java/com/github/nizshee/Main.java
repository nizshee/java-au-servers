package com.github.nizshee;


import com.github.nizshee.ui.Result;
import com.github.nizshee.ui.Tester;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("all")
public class Main {

    private static final Map<String, Message.Arch> archs = new HashMap<>();


    private static int requestCount = 50; // 10 100 10

    private static int clientCount = 50;
    private static int deltaValue = 50; // 10 100 10
    private static int sizeValue = 1000; // 400 2400 200

//    private static String host = "192.168.0.112";
    private static String host = "192.168.0.110";
    private static int port = 12347;

   static {
       archs.put("udpPool", Message.Arch.UdpPool);
       archs.put("tcpAsync", Message.Arch.TcpAssync);
       archs.put("tcpContThread", Message.Arch.TcpContThread);
       archs.put("tcpContNonblock", Message.Arch.TcpContNonblock);
       archs.put("tcpContPool", Message.Arch.TcpContPool);
       archs.put("tcpNew", Message.Arch.TcpNew);
       archs.put("udpThread", Message.Arch.UdpThread);
    }

    public static void main(String[] args) throws Exception {
        InetAddress serverHost = InetAddress.getByName("localhost");
        InetAddress clientHost = InetAddress.getByName("192.168.0.112");
        int serverPort = 12345;
        int clientPort = 12346;

        Tester tester = new Tester(serverHost, clientHost, serverPort, clientPort);

        run(tester, 20, 200, 20, Tester.Parameter.DELTA, "delta");
        run(tester, 20, 200, 20, Tester.Parameter.COUNT, "count");
        run(tester, 400, 2400, 200, Tester.Parameter.SIZE, "size");

    }

    private static void run(Tester tester, int from, int to, int step, Tester.Parameter parameter, String name) throws Exception {
        for (String arch: archs.keySet()) {
            List<Result> results = tester.start(from, to, step, parameter, archs.get(arch), requestCount,
                    clientCount, deltaValue, sizeValue, host, port);
            try {
                PrintWriter writerSort = new PrintWriter("results/" + arch + "_" + name + "_sort", "UTF-8");
                PrintWriter writerRequest = new PrintWriter("results/" + arch + "_" + name + "_request", "UTF-8");
                PrintWriter writerClient = new PrintWriter("results/" + arch + "_" + name + "_client", "UTF-8");
                writerSort.println(from + " " + to + " " + step);
                writerRequest.println(from + " " + to + " " + step);
                writerClient.println(from + " " + to + " " + step);
                writerSort.println(clientCount + " " + requestCount + " " + deltaValue + " " + sizeValue);
                writerRequest.println(clientCount + " " + requestCount + " " + deltaValue + " " + sizeValue);
                writerClient.println(clientCount + " " + requestCount + " " + deltaValue + " " + sizeValue);

                for (int i = 0; i < results.size(); ++i) {
                    writerSort.println(results.get(i).sort);
                    writerRequest.println(results.get(i).request);
                    writerClient.println(results.get(i).client);
                }

                writerSort.close();
                writerRequest.close();
                writerClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
