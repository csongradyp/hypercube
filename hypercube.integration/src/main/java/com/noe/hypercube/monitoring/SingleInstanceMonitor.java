package com.noe.hypercube.monitoring;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

/**
 * Monitor class for allocate running program instance.
 *
 * @author Peter_Csongrady
 */
public class SingleInstanceMonitor implements InstanceMonitor {

    private static final int BCK = 10;
    private static final int PORT = 12345;
    private ServerSocket socket;

    @Override
    public boolean isAlreadyRunning() {
        boolean isRunning = false;
        try {
            socket = new ServerSocket(PORT, BCK, InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            isRunning = true;
        } catch (IOException e) {
            isRunning = true;
        }
        return isRunning;
    }

    public ServerSocket getSocket() {
        return socket;
    }

}
