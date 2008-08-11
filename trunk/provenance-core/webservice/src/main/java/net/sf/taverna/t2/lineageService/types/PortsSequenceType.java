package net.sf.taverna.t2.lineageService.types;

public class PortsSequenceType  implements ProvenanceEventType {
    private PortType[] port;

    public PortsSequenceType() {
    }

    public PortsSequenceType(
           PortType[] port) {
           this.port = port;
    }


    /**
     * Gets the port value for this PortsSequenceType.
     * 
     * @return port
     */
    public PortType[] getPort() {
        return port;
    }


    /**
     * Sets the port value for this PortsSequenceType.
     * 
     * @param port
     */
    public void setPort(PortType[] port) {
        this.port = port;
    }

    public PortType getPort(int i) {
        return this.port[i];
    }

    public void setPort(int i, PortType _value) {
        this.port[i] = _value;
    }

}
