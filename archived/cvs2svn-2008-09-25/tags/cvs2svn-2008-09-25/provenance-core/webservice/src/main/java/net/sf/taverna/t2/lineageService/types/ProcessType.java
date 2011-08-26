package net.sf.taverna.t2.lineageService.types;

public class ProcessType  implements ProvenanceEventType {
    private ProcessorType[] processor;

    private java.lang.String dataflowID;  // attribute

    private java.lang.String facadeID;  // attribute

    public ProcessType() {
    }

    public ProcessType(
           ProcessorType[] processor,
           java.lang.String dataflowID,
           java.lang.String facadeID) {
           this.processor = processor;
           this.dataflowID = dataflowID;
           this.facadeID = facadeID;
    }


    /**
     * Gets the processor value for this ProcessType.
     * 
     * @return processor
     */
    public ProcessorType[] getProcessor() {
        return processor;
    }


    /**
     * Sets the processor value for this ProcessType.
     * 
     * @param processor
     */
    public void setProcessor(ProcessorType[] processor) {
        this.processor = processor;
    }

    public ProcessorType getProcessor(int i) {
        return this.processor[i];
    }

    public void setProcessor(int i, ProcessorType _value) {
        this.processor[i] = _value;
    }


    /**
     * Gets the dataflowID value for this ProcessType.
     * 
     * @return dataflowID
     */
    public java.lang.String getDataflowID() {
        return dataflowID;
    }


    /**
     * Sets the dataflowID value for this ProcessType.
     * 
     * @param dataflowID
     */
    public void setDataflowID(java.lang.String dataflowID) {
        this.dataflowID = dataflowID;
    }


    /**
     * Gets the facadeID value for this ProcessType.
     * 
     * @return facadeID
     */
    public java.lang.String getFacadeID() {
        return facadeID;
    }


    /**
     * Sets the facadeID value for this ProcessType.
     * 
     * @param facadeID
     */
    public void setFacadeID(java.lang.String facadeID) {
        this.facadeID = facadeID;
    }

}
