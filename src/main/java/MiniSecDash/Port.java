package MiniSecDash;

public class Port {
    private int portNumber;
    private String status; // "open", "closed", "filtered"

    public Port() {
        this.portNumber = -1;
        this.status = "unknown";
    }

    public Port(int portNumber, String status) {
        this.portNumber = portNumber;
        this.status = status;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}