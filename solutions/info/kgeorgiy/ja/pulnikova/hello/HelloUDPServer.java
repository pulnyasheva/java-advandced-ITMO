package info.kgeorgiy.ja.pulnikova.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class HelloUDPServer implements HelloServer {

    private ExecutorService executor;
    private DatagramSocket socket;

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("Invalid number of arguments");
            return;
        }
        try (HelloUDPServer helloUDPServer = new HelloUDPServer()) {
            int port = Integer.parseInt(args[0]);
            int threads = Integer.parseInt(args[1]);
            helloUDPServer.start(port, threads);
            Scanner scanner = new Scanner(System.in);
            scanner.next();
            scanner.close();
        } catch (NumberFormatException e) {
            System.err.println("Incorrect numeric values " + e.getMessage());
        }
    }

    @Override
    public void start(int port, int threads) {
        executor = newFixedThreadPool(threads);
        try {
            socket = new DatagramSocket(port);
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                        try {
                            byte[] receive = new byte[socket.getReceiveBufferSize()];
                            DatagramPacket packet = new DatagramPacket(new byte[0], 0);
                            Util.setByte(packet, receive);
                            socket.receive(packet);
                            String result = Util.packetForString(packet);
                            byte[] resultByte = Util.stringForByte(String.format("Hello, %s", result));
                            Util.setByte(packet, resultByte);
                            socket.send(packet);
                        } catch (IOException e) {
                            System.err.println("Problems with sending or receiving " + e.getMessage());
                        }
                    }
                });
            }
        } catch (SocketException e) {
            System.err.println("Problems with socket " + e.getMessage());
        }
    }

    @Override
    public void close() {
        socket.close();
        executor.shutdown();
    }
}
