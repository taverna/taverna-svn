/**
 * FetaCannedRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC3 Feb 28, 2005 (10:15:14 EST) WSDL2Java emitter.
 */

package uk.ac.man.cs.img.fetaEngine.webservice;

public class FetaCannedRequestType  implements java.io.Serializable {
    private uk.ac.man.cs.img.fetaEngine.webservice.CannedQueryType kindOfQuery;
    private java.lang.String paramValue;

    public FetaCannedRequestType() {
    }

    public FetaCannedRequestType(
           uk.ac.man.cs.img.fetaEngine.webservice.CannedQueryType kindOfQuery,
           java.lang.String paramValue) {
           this.kindOfQuery = kindOfQuery;
           this.paramValue = paramValue;
    }


    /**
     * Gets the kindOfQuery value for this FetaCannedRequestType.
     * 
     * @return kindOfQuery
     */
    public uk.ac.man.cs.img.fetaEngine.webservice.CannedQueryType getKindOfQuery() {
        return kindOfQuery;
    }


    /**
     * Sets the kindOfQuery value for this FetaCannedRequestType.
     * 
     * @param kindOfQuery
     */
    public void setKindOfQuery(uk.ac.man.cs.img.fetaEngine.webservice.CannedQueryType kindOfQuery) {
        this.kindOfQuery = kindOfQuery;
    }


    /**
     * Gets the paramValue value for this FetaCannedRequestType.
     * 
     * @return paramValue
     */
    public java.lang.String getParamValue() {
        return paramValue;
    }


    /**
     * Sets the paramValue value for this FetaCannedRequestType.
     * 
     * @param paramValue
     */
    public void setParamValue(java.lang.String paramValue) {
        this.paramValue = paramValue;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof FetaCannedRequestType)) return false;
        FetaCannedRequestType other = (FetaCannedRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.kindOfQuery==null && other.getKindOfQuery()==null) || 
             (this.kindOfQuery!=null &&
              this.kindOfQuery.equals(other.getKindOfQuery()))) &&
            ((this.paramValue==null && other.getParamValue()==null) || 
             (this.paramValue!=null &&
              this.paramValue.equals(other.getParamValue())));
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
        if (getKindOfQuery() != null) {
            _hashCode += getKindOfQuery().hashCode();
        }
        if (getParamValue() != null) {
            _hashCode += getParamValue().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(FetaCannedRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://mygrid.org.uk/2004/FETA", "fetaCannedRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("kindOfQuery");
        elemField.setXmlName(new javax.xml.namespace.QName("http://mygrid.org.uk/2004/FETA", "kindOfQuery"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://mygrid.org.uk/2004/FETA", "cannedQueryType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("paramValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://mygrid.org.uk/2004/FETA", "paramValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
