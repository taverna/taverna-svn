/**
 * FetaPublishResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC3 Feb 28, 2005 (10:15:14 EST) WSDL2Java emitter.
 */

package uk.ac.man.cs.img.fetaEngine.webservice;

public class FetaPublishResponseType  implements java.io.Serializable {
    private uk.ac.man.cs.img.fetaEngine.webservice.PublishResultType publishResult;
    private java.lang.String publishMessage;

    public FetaPublishResponseType() {
    }

    public FetaPublishResponseType(
           java.lang.String publishMessage,
           uk.ac.man.cs.img.fetaEngine.webservice.PublishResultType publishResult) {
           this.publishResult = publishResult;
           this.publishMessage = publishMessage;
    }


    /**
     * Gets the publishResult value for this FetaPublishResponseType.
     * 
     * @return publishResult
     */
    public uk.ac.man.cs.img.fetaEngine.webservice.PublishResultType getPublishResult() {
        return publishResult;
    }


    /**
     * Sets the publishResult value for this FetaPublishResponseType.
     * 
     * @param publishResult
     */
    public void setPublishResult(uk.ac.man.cs.img.fetaEngine.webservice.PublishResultType publishResult) {
        this.publishResult = publishResult;
    }


    /**
     * Gets the publishMessage value for this FetaPublishResponseType.
     * 
     * @return publishMessage
     */
    public java.lang.String getPublishMessage() {
        return publishMessage;
    }


    /**
     * Sets the publishMessage value for this FetaPublishResponseType.
     * 
     * @param publishMessage
     */
    public void setPublishMessage(java.lang.String publishMessage) {
        this.publishMessage = publishMessage;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof FetaPublishResponseType)) return false;
        FetaPublishResponseType other = (FetaPublishResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.publishResult==null && other.getPublishResult()==null) || 
             (this.publishResult!=null &&
              this.publishResult.equals(other.getPublishResult()))) &&
            ((this.publishMessage==null && other.getPublishMessage()==null) || 
             (this.publishMessage!=null &&
              this.publishMessage.equals(other.getPublishMessage())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getPublishResult() != null) {
            _hashCode += getPublishResult().hashCode();
        }
        if (getPublishMessage() != null) {
            _hashCode += getPublishMessage().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(FetaPublishResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://mygrid.org.uk/2004/FETA", "FetaPublishResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("publishResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://mygrid.org.uk/2004/FETA", "publishResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://mygrid.org.uk/2004/FETA", "publishResultType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("publishMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("http://mygrid.org.uk/2004/FETA", "publishMessage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
