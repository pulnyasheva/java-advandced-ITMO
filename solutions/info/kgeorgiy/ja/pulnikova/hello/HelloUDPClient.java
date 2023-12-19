package info.kgeorgiy.ja.pulnikova.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class HelloUDPClient implements HelloClient {

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Invalid number of arguments");
            return;
        }
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String prefix = args[2];
            int threads = Integer.parseInt(args[3]);
            int requests = Integer.parseInt(args[4]);
            HelloUDPClient helloUDPClient = new HelloUDPClient();
            helloUDPClient.run(host, port, prefix, threads, requests);
        } catch (NumberFormatException e) {
            System.err.println("Incorrect numeric values " + e.getMessage());
        }

    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        ExecutorService executor = newFixedThreadPool(threads);
        for (int i = 1; i <= threads; i++) {
            final int finalI = i;
            executor.submit(() -> {
                worker(prefix, requests, socketAddress, finalI);
            });
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(200L * threads * requests, TimeUnit.SECONDS)) {
                System.err.println("Threads could not terminate because the timeout expired");
            }
        } catch (InterruptedException e) {
            System.err.println("Problems with completion " + e.getMessage());
        }

    }

    private void worker(String prefix, int requests, SocketAddress socketAddress, int finalI) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(200);
            byte[] receive = new byte[socket.getReceiveBufferSize()];
            DatagramPacket packet = new DatagramPacket(new byte[0], 0, socketAddress);
            for (int j = 1; j <= requests; j++) {
                String requestString = String.format("%s%s_%s", prefix, finalI, j);
                byte[] request = Util.stringForByte(requestString);
                while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    try {
                        Util.setByte(packet, request);
                        socket.send(packet);
                        Util.setByte(packet, receive);
                        socket.receive(packet);
                    } catch (IOException e) {
                        System.err.println("Problems with sending or receiving " + e.getMessage());
                    }
                    String result = Util.packetForString(packet);
                    if (result.contains(requestString)) {
                        System.out.println(result);
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Problems with the DatagramSocket " + e.getMessage());
        }
    }
}
