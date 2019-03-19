package com.wjholden.tcpmonitor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JPanel;

public final class ConnectionMapPanel extends JPanel {
    
    private final ConnectionMap fireguard;
    private final AffineTransform transform;
    
    public ConnectionMapPanel() throws URISyntaxException, IOException {
        fireguard = new ConnectionMap();
        fireguard.start();
        transform = new AffineTransform();
        
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int) (ConnectionMap.WIDTH * transform.getScaleX()),
            (int) (ConnectionMap.HEIGHT * transform.getScaleY()));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        BufferedImage image = fireguard.renderImage();
        
        g2.setPaint(Color.BLACK);
        g2.fillRect(0, 0, (int) (ConnectionMap.WIDTH * transform.getScaleX()),
            (int) (ConnectionMap.HEIGHT * transform.getScaleY()));
        g2.drawImage(image, transform, null);
    }
    
    protected void setFrameSize(double x, double y) {
        transform.setToIdentity();
        transform.scale(x / ConnectionMap.WIDTH, y / ConnectionMap.HEIGHT);
    }
    
    protected void panX(int amount) {
        fireguard.xoffset = (fireguard.xoffset + amount + ConnectionMap.WIDTH) % ConnectionMap.WIDTH;
    }
    
    protected void panY(int amount) {
        fireguard.yoffset = (fireguard.yoffset + amount + ConnectionMap.HEIGHT) % ConnectionMap.HEIGHT;
    }
}
