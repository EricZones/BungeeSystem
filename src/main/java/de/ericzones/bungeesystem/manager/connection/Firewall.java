// Created by Eric B. 16.02.2021 16:44
package de.ericzones.bungeesystem.manager.connection;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;

public class Firewall {

    private static final String[] illegalAddresses = {"ovh.net", "gov.tl", "accelerate.net"};
    private static final String templateBlacklistCmd = "ipset -A proxies %ip%";

    private static final Runtime runtime = Runtime.getRuntime();

    public static void addToBlacklist(String ipAddress) {
        String cmd = templateBlacklistCmd.replace("%ip%", ipAddress);
        try {
            runtime.exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isProxy(String ipAddress) {
        try {
            URL url = new URL("http://proxycheck.io/v2/" + ipAddress + "&vpn=1");
            return containsYes(url);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private static boolean containsYes(URL url) {
        try (InputStream inputStream = url.openStream()) {
            return getInputStreamAsString(inputStream).contains("\"yes\"");
        } catch (IOException e) {
            return false;
        }
    }

    private static String getInputStreamAsString(InputStream inputStream) throws IOException {
        try (BufferedInputStream buff = new BufferedInputStream(inputStream); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            int result = buff.read();
            while(result != -1) {
                byte b = (byte) result;
                outputStream.write(b);
                result = buff.read();
            }
            return outputStream.toString();
        }
    }

    public static boolean isAllowed(InetSocketAddress socketAddress) {
        InetAddress ipAddress = socketAddress.getAddress();
        String address = ipAddress.getCanonicalHostName();
        for(String current : illegalAddresses) {
            if(address.contains(current)) return false;
        }
        return true;
    }

}
