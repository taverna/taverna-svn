/**
 * MetaDataService_ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.rapid_i.elico;

public class MetaDataService_ServiceLocator extends org.apache.axis.client.Service implements com.rapid_i.elico.MetaDataService_Service {

    public MetaDataService_ServiceLocator() {
    }


    public MetaDataService_ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public MetaDataService_ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for MetaDataServicePort
    private java.lang.String MetaDataServicePort_address = "http://rpc295.cs.man.ac.uk:8081/e-LICO/MetaDataService";

    public java.lang.String getMetaDataServicePortAddress() {
        return MetaDataServicePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String MetaDataServicePortWSDDServiceName = "MetaDataServicePort";

    public java.lang.String getMetaDataServicePortWSDDServiceName() {
        return MetaDataServicePortWSDDServiceName;
    }

    public void setMetaDataServicePortWSDDServiceName(java.lang.String name) {
        MetaDataServicePortWSDDServiceName = name;
    }

    public com.rapid_i.elico.MetaDataService_PortType getMetaDataServicePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(MetaDataServicePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getMetaDataServicePort(endpoint);
    }

    public com.rapid_i.elico.MetaDataService_PortType getMetaDataServicePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.rapid_i.elico.MetaDataServicePortBindingStub _stub = new com.rapid_i.elico.MetaDataServicePortBindingStub(portAddress, this);
            _stub.setPortName(getMetaDataServicePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setMetaDataServicePortEndpointAddress(java.lang.String address) {
        MetaDataServicePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.rapid_i.elico.MetaDataService_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.rapid_i.elico.MetaDataServicePortBindingStub _stub = new com.rapid_i.elico.MetaDataServicePortBindingStub(new java.net.URL(MetaDataServicePort_address), this);
                _stub.setPortName(getMetaDataServicePortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("MetaDataServicePort".equals(inputPortName)) {
            return getMetaDataServicePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://elico.rapid_i.com/", "MetaDataService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://elico.rapid_i.com/", "MetaDataServicePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("MetaDataServicePort".equals(portName)) {
            setMetaDataServicePortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
