package com.wjholden.tcpmonitor;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
