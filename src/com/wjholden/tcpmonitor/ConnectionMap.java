package com.wjholden.tcpmonitor;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GeoIP2-java library: https://github.com/maxmind/GeoIP2-java
 * GeoLite2 database: https://dev.maxmind.com/geoip/geoip2/geolite2/
 * 
 * 
 * @author William John Holden (https://wjholden.com)
 */
public final class ConnectionMap {
    
    // https://www.cisco.com/c/en/us/td/docs/security/asa/syslog/b_syslog/syslogs3.html
    // %ASA-6-302013: Built {inbound|outbound} TCP connection
    // %ASA-6-302014: Teardown TCP connection
    // %ASA-6-302015: Built {inbound|outbound} UDP connection
    // %ASA-6-302016: Teardown UDP connection
    private static final String OPEN = ".*302013.+outside:(.+?)/.*";
    private static final String CLOSE = ".*302014.+outside:(.+?)/.*";
    private static final Pattern OPEN_PATTERN = Pattern.compile(OPEN);
    private static final Pattern CLOSE_PATTERN = Pattern.compile(CLOSE);
    protected static final int WIDTH = 360 * 2;
    protected static final int HEIGHT = 180 * 2;

    private final Object lock = new Object();
    private final Queue<String> queue = new LinkedList<>();
    private final DatabaseReader reader;
    private final Thread syslogMulticastThread, syslogUnicastThread;
    private final Thread parseThread;
    private final Map<Map.Entry<Integer,Integer>, Integer> map;
    
    int xoffset = 0;
    int yoffset = 0;
    
    public void start() {
        syslogMulticastThread.start();
        //syslogUnicastThread.start();
        parseThread.start();
    }
    
    private void increment(Map.Entry<Integer,Integer> location) {
        int value = 1;
        if (map.containsKey(location)) {
            value += map.get(location);
        }
        map.put(location, value);
    }
    
    private void decrement(Map.Entry<Integer,Integer> location) {
        if (map.containsKey(location)) {
            int value = map.get(location);
            if (value == 1) {
                map.remove(location);
            } else {
                map.put(location, value - 1);
            }
        }
    }
    
    public ConnectionMap() throws URISyntaxException, IOException {
        //URL url = getClass().getResource("/resources/GeoLite2-City.mmdb");
        //File database = new File(url.toURI());
        //reader = new DatabaseReader.Builder(database).build();
        
        reader = new DatabaseReader.Builder(getClass().getResourceAsStream("/resources/GeoLite2-City.mmdb")).build();
        
        map = new ConcurrentHashMap<>();
        
        syslogMulticastThread = new Thread(new MulticastSyslogListener("239.5.1.4", 514, queue, lock));
        syslogMulticastThread.setDaemon(false);
        
        syslogUnicastThread = new Thread(new UnicastSyslogListener(514, queue, lock));
        syslogUnicastThread.setDaemon(true);
        
        parseThread = new Thread(() -> {
            while (true) {
                synchronized(lock) {
                    try {
                        lock.wait();
                        while (!queue.isEmpty()) {
                            String line = queue.remove();
                            Matcher openMatcher = OPEN_PATTERN.matcher(line);
                            if (openMatcher.matches()) {
                                Map.Entry<Integer,Integer> location = classify(getLocation(openMatcher.group(1)));
                                if (location != null) {
                                    increment(location);
                                }
                            } else {
                                Matcher closeMatcher = CLOSE_PATTERN.matcher(line);
                                if (closeMatcher.matches()) {
                                    Map.Entry<Integer,Integer> location = classify(getLocation(closeMatcher.group(1)));
                                    decrement(location);
                                }
                            }
                        }
                    } catch (InterruptedException ex) {
                        System.err.println(ex);
                        return;
                    } catch (GeoIp2Exception ex) {
                        System.err.println(ex);
                    }
                }
            }
        });
        parseThread.setDaemon(true);
    }
    
    private double[] getLocation(String address) throws GeoIp2Exception {
        try {
            InetAddress ipAddress = InetAddress.getByName(address);
            CityResponse response = reader.city(ipAddress);
            Location location = response.getLocation();
            return new double[] { location.getLatitude(), location.getLongitude() };
        } catch (IOException ex) {
            return null;
        }
    }
    
    private Map.Entry<Integer,Integer> classify(double[] location) {
        Integer latitude = (int)Math.round(location[0] + 90) * HEIGHT / 180;
        
        Integer longitude = (int)Math.round(location[1] + 180) * WIDTH / 360;
        
        return new AbstractMap.SimpleImmutableEntry<>(longitude, latitude);
    }
    
    public static void main(String[] args) throws UnknownHostException, URISyntaxException, IOException {
        ConnectionMap fireGuard = new ConnectionMap();
        fireGuard.start();
    }
    
    public BufferedImage renderImage() {
        final BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        if (!map.isEmpty()) {
            // yeah I hate it too, but we need to normalize values relative to each other.
            // int max = Collections.max(map.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getValue();
            
            map.forEach((location, intensity) -> {               
                // The heat map idea didn't work as well as I had hoped. For now,
                // this is just a simple white/black map of pixels.
                int x = location.getKey();
                int y = ConnectionMap.HEIGHT - location.getValue();
                image.setRGB((x + xoffset) % WIDTH, (y + yoffset) % HEIGHT, 0xffffffff);
            });
        }
        return image;
    }
}