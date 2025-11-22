package MiniSecDash;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Device {
    private String ipAddress;
    private String hostName;
    private boolean isNew = true;

    private ObservableList<Port> openPorts;

    public Device(String ipAddress, String hostName, boolean isNew) {
        this.ipAddress = ipAddress;
        this.hostName = hostName;
        this.openPorts = FXCollections.observableArrayList();
        this.isNew = isNew;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostName() {
        return hostName;
    }

    public boolean isNew() {
        return isNew;
    }

    public ObservableList<Port> getOpenPorts() {
        return openPorts;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public void addOpenPort(Port port) {
        openPorts.add(port);
    }
}
