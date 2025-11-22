package MiniSecDash;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NetworkGraph {
    private ObservableList<Device> devices;

    public NetworkGraph() {
        this.devices = FXCollections.observableArrayList();
    }

    public ObservableList<Device> getDevices() {
        return devices;
    }

    public void addDevice(Device device) {
        devices.add(device);
    }

    public void addDeviceSafe(Device device) {
        if (!devices.stream().anyMatch(d -> d.getIpAddress().equals(device.getIpAddress()))) {
            devices.add(device);
        }
    }
}