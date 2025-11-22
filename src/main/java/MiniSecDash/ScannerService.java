package MiniSecDash;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public class ScannerService {
    private NetworkGraph graph;
    private ExecutorService executor = Executors.newFixedThreadPool(50);
    private AtomicInteger completed = new AtomicInteger(0);
    private IntConsumer progressCallback;
    private final int poolSize = 50;

    public ScannerService() {
        graph = new NetworkGraph();
    }

    public ScannerService(NetworkGraph graph) {
        this.graph = graph;
    }

    public void scanIp(String ip) {
        ScannerTask task = new ScannerTask(ip, graph);
        // Wrap the task to report completion progress
        executor.submit(() -> {
            try {
                task.run();
            } finally {
                int done = completed.incrementAndGet();
                if (progressCallback != null) {
                    progressCallback.accept(done);
                }
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }

    // Accepts a "baseNetwork" string on startup
    public void startScan(String baseNetwork) {

        // Clear old devices
        graph.getDevices().clear();
        // reset progress counter
        completed.set(0);

        System.out.println("Starting scan on network: " + baseNetwork);

        // Scan the range (e.g., 192.168.1.1 to 192.168.1.254)
        for (int i = 1; i <= 254; i++) {
            // Build the IP from the user's input
            String ip = baseNetwork + "." + i;
            
            // Submit wrapped task through scanIp so progress is reported
            scanIp(ip);
        }
    }

    public void setProgressCallback(IntConsumer cb) {
        this.progressCallback = cb;
    }

    public void cancelScan() {
        // Attempt to stop running tasks and reset executor so future scans can start cleanly
        try {
            executor.shutdownNow();
        } catch (Exception ignore) {}
        // create a fresh executor for subsequent scans
        executor = Executors.newFixedThreadPool(poolSize);
        completed.set(0);
        if (progressCallback != null) {
            progressCallback.accept(0);
        }
    }
}