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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
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
    private static int GREEN = 0xff006000;

    private final Object lock = new Object();
    private final Queue<String> queue = new LinkedList<>();
    private final DatabaseReader reader;
    private final Thread syslogMulticastThread, syslogUnicastThread;
    private final Thread parseThread;
    private final Map<Map.Entry<Integer,Integer>, Integer> map;
    
    /**
     * See https://twitter.com/wjholdentech/status/1169124304501563394.
     * 
     * You can find the original "bw.jpeg" file that I started with in the project
     * resources. Here are the Julia commands I used to generate these files:
     * 
     * <pre>
     * {@code
     * using FileIO,Images,StatsBase
     * 
     * # Start with a 720x360 grayscale equirectangular world map (white = water)
     * bw = load("bw.jpeg")
     * 
     * # Verify that it fits in 720x360
     * size(bw)
     * 
     * # Observe that we do not have the true black/white pixels we expected
     * countmap(bw)
     * 
     * # The pixels are grayscale, with values between 0 and 1. Round them off.
     * bw_rounded = round.(bw)
     * countmap(bw_rounded)
     * 
     * # Get the indices of the non-white pixels, subtract one, and store as "x y" strings.
     * land = map(p -> string((p[2]-1)," ",(p[1]-1)), findall(x -> x != Gray{N0f8}(1.0), bw_rounded))
     * 
     * # Output the CSV as text
     * out = open("earth.txt","w")
     * foreach(line -> println(out, line), land)
     * close(out)
     * }
     * </pre>
     */
    private final Set<Integer> earth[] = new Set[3];
   
    /**
     * This is a useless "feature" that I wanted to leave in for fun.
     * To add a map of the earth I scraped an "equirectangular" map from 
     * Wikipedia. I knew the map wasn't grayscale, so I used a quick Julia
     * program to coerce grayscale. To my disappointment, either Julia or 
     * Gimp interpolated gray colors anyways, so I ended up with a map of the
     * earth with either too much land or too much water. You can press the
     * numbers 1, 2, and 3 on the keyboard to select which map you want.
     * 1 has too much land, 2 has too much water, and 3 is "just right" where
     * I rounded the grayscale pixels back to what they were supposed to be.
     */
    protected static int EARTH = 2;
    
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
        
        earth[0] = readMap("/resources/earth1.txt");
        earth[1] = readMap("/resources/earth2.txt");
        earth[2] = readMap("/resources/earth3.txt");
    }
    
    /**
     * Read a list of space-separated (x,y) coordinates from the earth.txt file
     * included as a project resource. Each (x,y) coordinate pair is composed into
     * a single integer. The 16 most significant bits represent x and the 16
     * least significant bits represent y. Thus, a different input file can be
     * used for resolutions up to 65536x65536 (!!!) without any code changes.
     * 
     * It is expected that the (x,y) coordinates are presented in the same domain
     * that this program renders output. Each pixel is considered "land", and each
     * pixel not presented is not considered land. Thus, the GUI needs only iterate
     * over all coordinates presented and color the corresponding pixel green.
     * 
     * @return an immutable set of integers that are the composition of (x,y)
     * coordinates in the GUI's pixel space representing land.
     */
    private Set<Integer> readMap(String resource) {
        final Set<Integer> e = new HashSet<>();
        
        Scanner sc = new Scanner(getClass().getResourceAsStream(resource));
        while (sc.hasNextInt()) {
            final int x = sc.nextInt();
            final int y = sc.nextInt();
            e.add((x << 16) | y);
        }
        
        return Collections.unmodifiableSet(e);
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
                
        earth[EARTH].forEach(pixel -> {
            final int x = pixel >> 16;
            final int y = pixel & 0x0000ffff;
            
            // I was surprised to learn that the alpha channel actually comes first.
            // The bytes of the integer are AA|RR|GG|BB.
            //
            // The magic numbers +20 and -1 are to correct a data problem where
            // pixels of the map are not correctly aligned with true lines of
            // latitude and longitude. Remove these constants if using a different map.
            image.setRGB((x + xoffset + 20) % WIDTH, (y + yoffset - 1) % HEIGHT, ConnectionMap.GREEN);
        });
        
        if (!map.isEmpty()) {
            // yeah I hate it too, but we need to normalize values relative to each other.
            // int max = Collections.max(map.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getValue();
            
            map.forEach((location, intensity) -> {               
                // The heat map idea didn't work as well as I had hoped. For now,
                // this is just a simple white/black map of pixels.
                final int x = location.getKey();
                final int y = ConnectionMap.HEIGHT - location.getValue();
                image.setRGB((x + xoffset) % WIDTH, (y + yoffset) % HEIGHT, 0xffffffff);
            });
        }
        
        return image;
    }
    
    protected static void moreGreen() {
        int g = (GREEN & 0x0000ff00) >>> 8;
        if (g < 0xff) {
            GREEN = 0xff000000 | ((g + 1) << 8);
            System.out.println(Integer.toString(g + 1, 16));
        }
    }
    
    protected static void lessGreen() {
        int g = (GREEN & 0x0000ff00) >>> 8;
        if (g > 0) {
            GREEN = 0xff000000 | ((g - 1) << 8);
            System.out.println(Integer.toString(g - 1, 16));
        }
    }
}