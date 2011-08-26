package net.sf.taverna.t2.lineageService.types;

public class LiteralType  implements ProvenanceEventType {
    private java.lang.String id;  // attribute

    public LiteralType() {
    }

    public LiteralType(
           java.lang.String id) {
           this.id = id;
    }


    /**
     * Gets the id value for this LiteralType.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this LiteralType.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }

}
