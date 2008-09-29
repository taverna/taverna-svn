/**
 * FetaPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC3 Feb 28, 2005 (10:15:14 EST) WSDL2Java emitter.
 */

package uk.ac.man.cs.img.fetaEngine.webservice;

public interface FetaPortType extends java.rmi.Remote {
    public uk.ac.man.cs.img.fetaEngine.webservice.FetaSearchResponseType inquire(uk.ac.man.cs.img.fetaEngine.webservice.FetaCompositeSearchRequestType searchRequest) throws java.rmi.RemoteException;
    public uk.ac.man.cs.img.fetaEngine.webservice.FetaPublishResponseType publishDescription(java.lang.String publicationRequest) throws java.rmi.RemoteException;
    public uk.ac.man.cs.img.fetaEngine.webservice.FetaRemoveResponseType removeDescription(java.lang.String removalRequest) throws java.rmi.RemoteException;
    public java.lang.String freeFormQuery(java.lang.String freeQueryIn) throws java.rmi.RemoteException;
}
