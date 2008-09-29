/**
 * FetaLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC3 Feb 28, 2005 (10:15:14 EST) WSDL2Java emitter.
 */

package uk.ac.man.cs.img.fetaEngine.webservice;

public class FetaLocator extends org.apache.axis.client.Service implements uk.ac.man.cs.img.fetaEngine.webservice.Feta {

    public FetaLocator() {
    }


    public FetaLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public FetaLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for fetaAdmin
    private java.lang.String fetaAdmin_address = "http://localhost:8080/fetaEngine/services/fetaAdmin";

    public java.lang.String getfetaAdminAddress() {
        return fetaAdmin_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String fetaAdminWSDDServiceName = "fetaAdmin";

    public java.lang.String getfetaAdminWSDDServiceName() {
        return fetaAdminWSDDServiceName;
    }

    public void setfetaAdminWSDDServiceName(java.lang.String name) {
        fetaAdminWSDDServiceName = name;
    }

    public uk.ac.man.cs.img.fetaEngine.webservice.FetaAdminPortType getfetaAdmin() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(fetaAdmin_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getfetaAdmin(endpoint);
    }

    public uk.ac.man.cs.img.fetaEngine.webservice.FetaAdminPortType getfetaAdmin(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            uk.ac.man.cs.img.fetaEngine.webservice.FetaAdminPortTypeBindingStub _stub = new uk.ac.man.cs.img.fetaEngine.webservice.FetaAdminPortTypeBindingStub(portAddress, this);
            _stub.setPortName(getfetaAdminWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setfetaAdminEndpointAddress(java.lang.String address) {
        fetaAdmin_address = address;
    }


    // Use to get a proxy class for feta
    private java.lang.String feta_address = "http://localhost:8080/fetaEngine/services/feta";

    public java.lang.String getfetaAddress() {
        return feta_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String fetaWSDDServiceName = "feta";

    public java.lang.String getfetaWSDDServiceName() {
        return fetaWSDDServiceName;
    }

    public void setfetaWSDDServiceName(java.lang.String name) {
        fetaWSDDServiceName = name;
    }

    public uk.ac.man.cs.img.fetaEngine.webservice.FetaPortType getfeta() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(feta_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getfeta(endpoint);
    }

    public uk.ac.man.cs.img.fetaEngine.webservice.FetaPortType getfeta(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            uk.ac.man.cs.img.fetaEngine.webservice.FetaPortTypeBindingStub _stub = new uk.ac.man.cs.img.fetaEngine.webservice.FetaPortTypeBindingStub(portAddress, this);
            _stub.setPortName(getfetaWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setfetaEndpointAddress(java.lang.String address) {
        feta_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (uk.ac.man.cs.img.fetaEngine.webservice.FetaAdminPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                uk.ac.man.cs.img.fetaEngine.webservice.FetaAdminPortTypeBindingStub _stub = new uk.ac.man.cs.img.fetaEngine.webservice.FetaAdminPortTypeBindingStub(new java.net.URL(fetaAdmin_address), this);
                _stub.setPortName(getfetaAdminWSDDServiceName());
                return _stub;
            }
            if (uk.ac.man.cs.img.fetaEngine.webservice.FetaPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                uk.ac.man.cs.img.fetaEngine.webservice.FetaPortTypeBindingStub _stub = new uk.ac.man.cs.img.fetaEngine.webservice.FetaPortTypeBindingStub(new java.net.URL(feta_address), this);
                _stub.setPortName(getfetaWSDDServiceName());
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
        if ("fetaAdmin".equals(inputPortName)) {
            return getfetaAdmin();
        }
        else if ("feta".equals(inputPortName)) {
            return getfeta();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://mygrid.org.uk/2004/FETA", "feta");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://mygrid.org.uk/2004/FETA", "fetaAdmin"));
            ports.add(new javax.xml.namespace.QName("http://mygrid.org.uk/2004/FETA", "feta"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        if ("fetaAdmin".equals(portName)) {
            setfetaAdminEndpointAddress(address);
        }
        if ("feta".equals(portName)) {
            setfetaEndpointAddress(address);
        }
        else { // Unknown Port Name
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
