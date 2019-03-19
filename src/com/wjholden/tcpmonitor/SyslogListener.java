package com.wjholden.tcpmonitor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Queue;

public abstract class SyslogListener implements Runnable {
    protected final Queue<String> queue;
    protected final Object lock;
    protected final int port;

    public SyslogListener(Queue<String> queue, Object lock, int port) {
        this.queue = queue;
        this.lock = lock;
        this.port = port;
    }
    
    protected void receive(DatagramSocket socket) throws IOException {
        byte[] buf = new byte[1500];    
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        while (true) {
            socket.receive(packet);
            String message = new String(buf, 0, packet.getLength() - 1);
            synchronized(lock) {
                queue.add(message);
                lock.notifyAll();
            }
        }
    }
}
