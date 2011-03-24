/**
 * AnnotationsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.rapid_i.elico;

public class AnnotationsResponse  implements java.io.Serializable {
    private com.rapid_i.elico.AnnotationsResponseAnnotationsEntry[] annotations;

    private java.lang.String errorMessage;

    public AnnotationsResponse() {
    }

    public AnnotationsResponse(
           com.rapid_i.elico.AnnotationsResponseAnnotationsEntry[] annotations,
           java.lang.String errorMessage) {
           this.annotations = annotations;
           this.errorMessage = errorMessage;
    }


    /**
     * Gets the annotations value for this AnnotationsResponse.
     * 
     * @return annotations
     */
    public com.rapid_i.elico.AnnotationsResponseAnnotationsEntry[] getAnnotations() {
        return annotations;
    }


    /**
     * Sets the annotations value for this AnnotationsResponse.
     * 
     * @param annotations
     */
    public void setAnnotations(com.rapid_i.elico.AnnotationsResponseAnnotationsEntry[] annotations) {
        this.annotations = annotations;
    }


    /**
     * Gets the errorMessage value for this AnnotationsResponse.
     * 
     * @return errorMessage
     */
    public java.lang.String getErrorMessage() {
        return errorMessage;
    }


    /**
     * Sets the errorMessage value for this AnnotationsResponse.
     * 
     * @param errorMessage
     */
    public void setErrorMessage(java.lang.String errorMessage) {
        this.errorMessage = errorMessage;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AnnotationsResponse)) return false;
        AnnotationsResponse other = (AnnotationsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.annotations==null && other.getAnnotations()==null) || 
             (this.annotations!=null &&
              java.util.Arrays.equals(this.annotations, other.getAnnotations()))) &&
            ((this.errorMessage==null && other.getErrorMessage()==null) || 
             (this.errorMessage!=null &&
              this.errorMessage.equals(other.getErrorMessage())));
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
        if (getAnnotations() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAnnotations());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAnnotations(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getErrorMessage() != null) {
            _hashCode += getErrorMessage().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AnnotationsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://elico.rapid_i.com/", "annotationsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("annotations");
        elemField.setXmlName(new javax.xml.namespace.QName("", "annotations"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://elico.rapid_i.com/", ">>annotationsResponse>annotations>entry"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("", "entry"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("", "errorMessage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
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
