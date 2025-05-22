package fr.cpe.mineprojetiot;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class MyThreadReceiver extends Thread{
    private MyThreadEventListener listener;
    private boolean running = true;
    private DatagramSocket UDPSocket;
    private InetAddress address;
    private String IP;

    private int PORT;


    public MyThreadReceiver(MyThreadEventListener listener, String ip,int port) {
        this.listener = listener;
        try {
            this.UDPSocket = new DatagramSocket();
            UDPSocket.setSoTimeout(10000);
            this.address = InetAddress.getByName(ip);
            this.IP = ip;
            this.PORT = port;
        } catch (IOException e) {  }
    }

    public void stopThread() {
        running = false;
        UDPSocket.close();
    }

    public void run() {
        while (running) {
            try {
                // Send the update packet
                String update = "getValues()";
                byte[] updateBuffer = update.getBytes();
                DatagramPacket updatePacket = new DatagramPacket(updateBuffer, updateBuffer.length, this.address, this.PORT);
                UDPSocket.send(updatePacket);

                // Wait for a response
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    UDPSocket.receive(packet); // Will block up to 10 seconds
                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Received message: " + message);
                    listener.onEventInMyThread(message);
                } catch (SocketTimeoutException e) {
                    System.out.println("No response received after 10 seconds. Retrying...");
                    // You can log or handle retries here
                }
                sleep(2000);
            } catch (IOException | InterruptedException e) { e.printStackTrace(); }
        }
    }
}
