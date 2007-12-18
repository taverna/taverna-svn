/**
 * Feta.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC3 Feb 28, 2005 (10:15:14 EST) WSDL2Java emitter.
 */

package uk.ac.man.cs.img.fetaEngine.webservice;

public interface Feta extends javax.xml.rpc.Service {
    public java.lang.String getfetaAdminAddress();

    public uk.ac.man.cs.img.fetaEngine.webservice.FetaAdminPortType getfetaAdmin() throws javax.xml.rpc.ServiceException;

    public uk.ac.man.cs.img.fetaEngine.webservice.FetaAdminPortType getfetaAdmin(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
    public java.lang.String getfetaAddress();

    public uk.ac.man.cs.img.fetaEngine.webservice.FetaPortType getfeta() throws javax.xml.rpc.ServiceException;

    public uk.ac.man.cs.img.fetaEngine.webservice.FetaPortType getfeta(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
