package fr.cpe.mineprojetiot;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
            this.address = InetAddress.getByName(ip);
            this.IP = ip;
            this.PORT = port;
            System.out.println("Receiver started on " + ip + ":" + port + " " + this.UDPSocket);

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
                System.out.println("Sending update packet " + update + " " + updateBuffer);
                UDPSocket.send(updatePacket);

                // Wait for a response
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                UDPSocket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                // Process the received message
                System.out.println("Received message: " + message);
                listener.onEventInMyThread(message);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}
