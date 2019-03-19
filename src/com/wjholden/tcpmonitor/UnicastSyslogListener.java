package com.wjholden.tcpmonitor;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Queue;

public class UnicastSyslogListener extends SyslogListener {
    
    public UnicastSyslogListener(int port, Queue<String> queue, Object lock) {
        super(queue, lock, port);
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            super.receive(socket);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
    
}
