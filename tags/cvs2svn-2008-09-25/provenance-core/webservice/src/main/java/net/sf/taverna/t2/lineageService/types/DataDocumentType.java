package net.sf.taverna.t2.lineageService.types;

public class DataDocumentType  implements ProvenanceEventType {
    private java.lang.String reference;

    private java.lang.String id;  // attribute

    public DataDocumentType() {
    }

    public DataDocumentType(
           java.lang.String reference,
           java.lang.String id) {
           this.reference = reference;
           this.id = id;
    }


    /**
     * Gets the reference value for this DataDocumentType.
     * 
     * @return reference
     */
    public java.lang.String getReference() {
        return reference;
    }


    /**
     * Sets the reference value for this DataDocumentType.
     * 
     * @param reference
     */
    public void setReference(java.lang.String reference) {
        this.reference = reference;
    }


    /**
     * Gets the id value for this DataDocumentType.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this DataDocumentType.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }

}
