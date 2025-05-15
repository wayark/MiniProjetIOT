package fr.cpe.mineprojetiot;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MyThreadSender extends Thread {

    private String IP;
    private int PORT;
    private BlockingQueue<String> queue;
    private InetAddress address;
    private DatagramSocket UDPSocket;


    public MyThreadSender(String ip, int port) {
        this.queue = new LinkedBlockingQueue<>(10);
        try {
            this.IP = ip;
            this.PORT = port;
            this.UDPSocket = new DatagramSocket();
            this.address = InetAddress.getByName(IP);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void sendMessage(String message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) { e.printStackTrace(); }
    }


    public void run() {
        while (true) {
            try {
                String message = queue.take();

                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.address, this.PORT);
                try {
                    UDPSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
