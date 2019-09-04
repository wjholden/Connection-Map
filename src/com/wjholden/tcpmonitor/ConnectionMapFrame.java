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
    
    private static final int TIMER_DELAY_MILLISECONDS = 1000;
    private ConnectionMapPanel panel;
    private final Timer timer;
    
    /**
     * First there was an unpleasant snap after the initial timer delay.
     * Now there is a weird artifact at the bottom of the screen when maximized.
     * There is some misalignment in the padding between the JFrame and JPanel.
     */
    private static final int PADDING = 10;
    
    private final static String HELP
            = "a: show about\n"
            + "f: toggle fullscreen\n"
            + "h: show this help\n"
            + "q: quit\n"
            + "\u2191\u2193\u2190\u2192: pan everything\n"
            + "wasd: pan map only\n"
            + "1-3: select map\n"
            + "g: darken green\n"
            + "G: brighten green";
    
    public ConnectionMapFrame() {
        setLayout(new FlowLayout());
        timer = new javax.swing.Timer(TIMER_DELAY_MILLISECONDS, this::updateScreen);
        
        addComponentListener(new ComponentAdapter() {
           @Override
           public void componentResized(ComponentEvent e) {
               panel.setFrameSize(getContentPane().getWidth() - PADDING, getContentPane().getHeight() - PADDING);
               panel.repaint();
           }
        });
        
        addWindowStateListener((WindowEvent e) -> {
            panel.setFrameSize(getWidth() - PADDING, getHeight() - PADDING);
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
                break;
            case 'h':
                JOptionPane.showMessageDialog(this, HELP, "Help", JOptionPane.QUESTION_MESSAGE);
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
