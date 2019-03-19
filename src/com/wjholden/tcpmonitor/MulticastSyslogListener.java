package com.wjholden.tcpmonitor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Queue;

public class MulticastSyslogListener extends SyslogListener {
    
    private final InetAddress group;
    
    public MulticastSyslogListener(String group, int port, Queue<String> queue, Object lock) throws UnknownHostException {
        super(queue, lock, port);
        this.group = InetAddress.getByName(group);
    }

    @Override
    public void run() {
        try (MulticastSocket socket = new MulticastSocket(port)) {
            socket.joinGroup(group);
            super.receive(socket);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
