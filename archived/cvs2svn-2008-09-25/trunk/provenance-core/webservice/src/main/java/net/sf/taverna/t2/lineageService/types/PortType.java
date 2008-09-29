package net.sf.taverna.t2.lineageService.types;

public class PortType  implements ProvenanceEventType {
    private DataDocumentType dataDocument;

    private LiteralType literal;

    private java.lang.String name;  // attribute

    public PortType() {
    }

    public PortType(
           DataDocumentType dataDocument,
           LiteralType literal,
           java.lang.String name) {
           this.dataDocument = dataDocument;
           this.literal = literal;
           this.name = name;
    }


    /**
     * Gets the dataDocument value for this PortType.
     * 
     * @return dataDocument
     */
    public DataDocumentType getDataDocument() {
        return dataDocument;
    }


    /**
     * Sets the dataDocument value for this PortType.
     * 
     * @param dataDocument
     */
    public void setDataDocument(DataDocumentType dataDocument) {
        this.dataDocument = dataDocument;
    }


    /**
     * Gets the literal value for this PortType.
     * 
     * @return literal
     */
    public LiteralType getLiteral() {
        return literal;
    }


    /**
     * Sets the literal value for this PortType.
     * 
     * @param literal
     */
    public void setLiteral(LiteralType literal) {
        this.literal = literal;
    }


    /**
     * Gets the name value for this PortType.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this PortType.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

}
