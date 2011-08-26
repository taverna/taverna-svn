package net.sf.taverna.t2.lineageService.types;

public class IterationType  implements ProvenanceEventType {
    private PortsSequenceType inputdata;

    private PortsSequenceType outputdata;

    private java.lang.String id;  // attribute

    public IterationType() {
    }

    public IterationType(
           PortsSequenceType inputdata,
           PortsSequenceType outputdata,
           java.lang.String id) {
           this.inputdata = inputdata;
           this.outputdata = outputdata;
           this.id = id;
    }


    /**
     * Gets the inputdata value for this IterationType.
     * 
     * @return inputdata
     */
    public PortsSequenceType getInputdata() {
        return inputdata;
    }


    /**
     * Sets the inputdata value for this IterationType.
     * 
     * @param inputdata
     */
    public void setInputdata(PortsSequenceType inputdata) {
        this.inputdata = inputdata;
    }


    /**
     * Gets the outputdata value for this IterationType.
     * 
     * @return outputdata
     */
    public PortsSequenceType getOutputdata() {
        return outputdata;
    }


    /**
     * Sets the outputdata value for this IterationType.
     * 
     * @param outputdata
     */
    public void setOutputdata(PortsSequenceType outputdata) {
        this.outputdata = outputdata;
    }


    /**
     * Gets the id value for this IterationType.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this IterationType.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }

}
