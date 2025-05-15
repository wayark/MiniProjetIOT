package fr.cpe.mineprojetiot;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MyThreadReceiver extends Thread{
    private MyThreadEventListener listener;
    private boolean running = true;
    private DatagramSocket UDPSocket;

    public MyThreadReceiver(MyThreadEventListener listener, int port) {
        this.listener = listener;
        try {
            this.UDPSocket = new DatagramSocket(port);
        } catch (IOException e) {  }
    }

    public void stopThread() {
        running = false;
    }

    public void run() {
        while (running) {
            if(UDPSocket == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) { e.printStackTrace(); }
                continue;
            }
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                UDPSocket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                listener.onEventInMyThread(message);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}
