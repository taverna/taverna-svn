/**
 * MetaDataService_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.rapid_i.elico;

public interface MetaDataService_PortType extends java.rmi.Remote {
    public com.rapid_i.elico.AnnotationsResponse getAnnotations(java.lang.String location) throws java.rmi.RemoteException;
    public com.rapid_i.elico.AnnotationsResponse setAnnotation(java.lang.String location, java.lang.String annotationName, java.lang.String annotationValue) throws java.rmi.RemoteException;
    public com.rapid_i.elico.AnnotationsResponse deleteAnnotation(java.lang.String location, java.lang.String annotationKey) throws java.rmi.RemoteException;
    public com.rapid_i.elico.DataTableResponse getOWLIndividualsFromOperator(java.lang.String baseURL, java.lang.String owlPrefix, java.lang.String sourceURL, java.lang.String operatorName, com.rapid_i.elico.OperatorParameter[] operatorParameters) throws java.rmi.RemoteException;
    public com.rapid_i.elico.DataTableResponse getOWLIndividualsFromURL(java.lang.String baseURL, java.lang.String owlPrefix, java.lang.String sourceURL) throws java.rmi.RemoteException;
    public com.rapid_i.elico.DataTableResponse getOWLIndividualsFromRepository(java.lang.String baseURL, java.lang.String owlPrefix, java.lang.String location) throws java.rmi.RemoteException;
}
