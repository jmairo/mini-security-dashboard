package MiniSecDash;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import javafx.application.Platform;

public class ScannerTask implements Runnable {
    private String ip;
    private NetworkGraph graph;

    public ScannerTask() {
        ip = "0.0.0.0";
        graph = new NetworkGraph();
    }

    public ScannerTask(String ip, NetworkGraph graph) {
        this.ip = ip;
        this.graph = graph;
    }

    @Override
    public void run() {
    // A list of common ports to check
    int[] ports = { 22, 53, 80, 135, 443, 445, 3389, 8080 }; 
    
    Device device = new Device(ip, "Unknown Host", true);
    boolean hostIsUp = false;

    for (int port : ports) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), 500); // 500ms timeout
            
            // Port is open
            device.addOpenPort(new Port(port, "open"));
            hostIsUp = true; // Found the host

        } catch (Exception e) {
            // This port is closed, just continue to the next one
        }
    }

    // If any open port is found, add the device
    if (hostIsUp) {
        String hostName = "Unknown Host";
        try {
            hostName = InetAddress.getByName(ip).getHostName();
        } catch (Exception e) {
            // ignore, keep Unknown Host
        }

        // update device's host name and publish to UI thread
        device.setHostName(hostName);

        Platform.runLater(() -> graph.addDeviceSafe(device));
    }
}
}