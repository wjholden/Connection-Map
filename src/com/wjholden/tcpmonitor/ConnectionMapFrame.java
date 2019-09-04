package com.wjholden.tcpmonitor;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class ConnectionMapFrame extends JFrame implements KeyListener {
    
    private ConnectionMapPanel panel;
    private final Timer timer;
    
    public ConnectionMapFrame() {
        setLayout(new FlowLayout());
        timer = new javax.swing.Timer(1000, this::updateScreen);
        
        addComponentListener(new ComponentAdapter() {
           @Override
           public void componentResized(ComponentEvent e) {
               panel.setFrameSize(getWidth(), getHeight());
           }
        });
        
        addWindowStateListener((WindowEvent e) -> {
            panel.setFrameSize(getWidth(), getHeight());
            panel.repaint();
        });
    }
    
    private void updateScreen(ActionEvent e) {
        panel.repaint();
    }
    
    private static void createAndShowGui() {
        ConnectionMapFrame f = new ConnectionMapFrame();
        try {
            f.panel = new ConnectionMapPanel();
        } catch (URISyntaxException | IOException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        f.getContentPane().setBackground(Color.BLACK);
        f.add(f.panel);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        f.addKeyListener(f);
        f.timer.start();
        f.setTitle("Connection Map");
    }
    
    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> createAndShowGui());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 'q':
                System.exit(0);
                break;
            case 'f':
                setFullscreen();
                break;
            case '1':
                ConnectionMap.EARTH = 0;
                break;
            case '2':
                ConnectionMap.EARTH = 1;
                break;
            case '3':
                ConnectionMap.EARTH = 2;
                break;
            case 'g':
                ConnectionMap.lessGreen();
                break;
            case 'G':
                ConnectionMap.moreGreen();
                break;
            case 'w':
                ConnectionMap.mapOffsetY(-1);
                break;
            case 's':
                ConnectionMap.mapOffsetY(+1);
                break;
            case 'a':
                ConnectionMap.mapOffsetX(-1);
                break;
            case 'd':
                ConnectionMap.mapOffsetX(+1);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                panel.panX(-1);
                break;
            case KeyEvent.VK_RIGHT:
                panel.panX(1);
                break;
            case KeyEvent.VK_UP:
                panel.panY(-1);
                break;
            case KeyEvent.VK_DOWN:
                panel.panY(1);
                break;
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        
    }
    
    // taken from my own project at https://github.com/wjholden/Route-Monitor/blob/master/Route%20Monitor/src/com/wjholden/routemonitor/SupernetFrame.java#L107
    private void setFullscreen() {
        this.dispose();
        if (isUndecorated()) {
            // clear the maximized bits (4 and 2) if fullscreen -> normal
            setExtendedState(getExtendedState() & (~JFrame.MAXIMIZED_BOTH));
            setUndecorated(false);
        } else {
            // set the maximized bits if normal -> fullscreen
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
            setUndecorated(true);
        }
        this.setLocation(0, 0);
        this.pack();
        this.setVisible(true);
        
        panel.setFrameSize(getWidth(), getHeight());
    }
}
