/**
 * CannedQueryType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC3 Feb 28, 2005 (10:15:14 EST) WSDL2Java emitter.
 */

package uk.ac.man.cs.img.fetaEngine.webservice;

public class CannedQueryType implements java.io.Serializable {
    private org.apache.axis.types.NMToken _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected CannedQueryType(org.apache.axis.types.NMToken value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final org.apache.axis.types.NMToken _ByName = new org.apache.axis.types.NMToken("ByName");
    public static final org.apache.axis.types.NMToken _ByDescription = new org.apache.axis.types.NMToken("ByDescription");
    public static final org.apache.axis.types.NMToken _ByInput = new org.apache.axis.types.NMToken("ByInput");
    public static final org.apache.axis.types.NMToken _ByOutput = new org.apache.axis.types.NMToken("ByOutput");
    public static final org.apache.axis.types.NMToken _ByApplication = new org.apache.axis.types.NMToken("ByApplication");
    public static final org.apache.axis.types.NMToken _ByMethod = new org.apache.axis.types.NMToken("ByMethod");
    public static final org.apache.axis.types.NMToken _ByTask = new org.apache.axis.types.NMToken("ByTask");
    public static final org.apache.axis.types.NMToken _ByResource = new org.apache.axis.types.NMToken("ByResource");
    public static final org.apache.axis.types.NMToken _ByResourceContent = new org.apache.axis.types.NMToken("ByResourceContent");
    public static final org.apache.axis.types.NMToken _ByType = new org.apache.axis.types.NMToken("ByType");
    public static final org.apache.axis.types.NMToken _GetAll = new org.apache.axis.types.NMToken("GetAll");
    public static final CannedQueryType ByName = new CannedQueryType(_ByName);
    public static final CannedQueryType ByDescription = new CannedQueryType(_ByDescription);
    public static final CannedQueryType ByInput = new CannedQueryType(_ByInput);
    public static final CannedQueryType ByOutput = new CannedQueryType(_ByOutput);
    public static final CannedQueryType ByApplication = new CannedQueryType(_ByApplication);
    public static final CannedQueryType ByMethod = new CannedQueryType(_ByMethod);
    public static final CannedQueryType ByTask = new CannedQueryType(_ByTask);
    public static final CannedQueryType ByResource = new CannedQueryType(_ByResource);
    public static final CannedQueryType ByResourceContent = new CannedQueryType(_ByResourceContent);
    public static final CannedQueryType ByType = new CannedQueryType(_ByType);
    public static final CannedQueryType GetAll = new CannedQueryType(_GetAll);
    public org.apache.axis.types.NMToken getValue() { return _value_;}
    public static CannedQueryType fromValue(org.apache.axis.types.NMToken value)
          throws java.lang.IllegalArgumentException {
        CannedQueryType enumeration = (CannedQueryType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static CannedQueryType fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        try {
            return fromValue(new org.apache.axis.types.NMToken(value));
        } catch (Exception e) {
            throw new java.lang.IllegalArgumentException();
        }
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_.toString();}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CannedQueryType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://mygrid.org.uk/2004/FETA", "cannedQueryType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
