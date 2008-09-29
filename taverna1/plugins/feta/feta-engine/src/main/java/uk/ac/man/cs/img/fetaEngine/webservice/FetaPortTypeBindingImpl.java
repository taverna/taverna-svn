/*
 *
 * Copyright (C) 2006 The University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 */

package uk.ac.man.cs.img.fetaEngine.webservice;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Properties;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import org.apache.log4j.Logger;


import uk.ac.man.cs.img.fetaEngine.store.IFetaModel;
import uk.ac.man.cs.img.fetaEngine.store.FetaEngineException;
import uk.ac.man.cs.img.fetaEngine.store.FetaEngineProperties;
import uk.ac.man.cs.img.fetaEngine.store.PedroXMLToRDF;
import uk.ac.man.cs.img.fetaEngine.store.load.FetaLoad;
import uk.ac.man.cs.img.fetaEngine.store.load.FetaLoadException;
import uk.ac.man.cs.img.fetaEngine.store.load.FetaSourceRepository;

import org.w3c.dom.Document;

public class FetaPortTypeBindingImpl implements uk.ac.man.cs.img.fetaEngine.webservice.FetaPortType{


	private IFetaModel feta;
	static Logger logger = Logger.getLogger(FetaPortTypeBindingImpl.class);
	FetaEngineProperties props;
	////constructor
	public FetaPortTypeBindingImpl() throws RemoteException {
                super();

                System.out.println("Debug in FetaPortTypeBindingImpl");
                logger.debug("Instantiated Binding Implementation");

                try {
                         props = new FetaEngineProperties();
                }catch (FetaEngineException ex) {
                        throw new RemoteException("Problem loading configuration", ex);
                }catch (Exception e) {
                        throw new RemoteException("Problem loading configuration", e);
                }

                try {
                        //load the provider info from properties file and instantiate
                        String className = props.getPropertyValue("fetaEngine.backend.provider",
                                "uk.ac.man.cs.img.fetaEngine.store.impl.sesame2.SesameModelImpl");
                        System.out.println("RDF Provider class name is "+className);
                        feta = (IFetaModel) Class.forName(className).getDeclaredMethod("getInstance", new Class[] {}).invoke(null, new Object[] {});
                        System.out.println("Get Instance Invoked-Done");
                } catch (ClassNotFoundException e) {
                                throw new RemoteException("Problem connecting to fetaEngine", e);
                } catch (InvocationTargetException e) {
                                throw new RemoteException("Problem connecting to fetaEngine", e);
                } catch (NoSuchMethodException e) {
                                throw new RemoteException("Problem connecting to fetaEngine", e);
                } catch (IllegalAccessException e) {
                                throw new RemoteException("Problem connecting to fetaEngine", e);
                }

	}


	public uk.ac.man.cs.img.fetaEngine.webservice.FetaSearchResponseType inquire(uk.ac.man.cs.img.fetaEngine.webservice.FetaCompositeSearchRequestType searchRequest) throws java.rmi.RemoteException {
            
        System.out.println("+++++++++THIS IS INQUIRE++++++++++++");
		IFetaModel fetta = getStore();
		Set resultSet = new HashSet();
		boolean prelim = true;
                FetaSearchResponseType results;

                try {
                        uk.ac.man.cs.img.fetaEngine.webservice.FetaCannedRequestType[]   cannedQueries = searchRequest.getFetaAtomicCannedRequest();
                        for (int s = 0; s < cannedQueries.length; s++) {

                                CannedQueryType queryType = cannedQueries[s].getKindOfQuery();
                                String paramValue = cannedQueries[s].getParamValue();
                                Set cannedQueryResults = fetta.cannedQuery(queryType/*.getValue()*/, paramValue);
                                if (cannedQueryResults.isEmpty()) {
                                        resultSet = new HashSet();
                                        prelim = false;
                                } else {
                                        this.appendServiceSet(resultSet, cannedQueryResults, prelim);
                                        prelim = false;
                                        logger.debug("The canned query is of type: " + queryType.toString());
                                }
                        }
                        results = copyToArray(resultSet);
		} catch (FetaEngineException e) {
						e.printStackTrace();
			throw new RemoteException("problem during query execution in Feta engine", e);
		}
				return  results;
	}

	public java.lang.String freeFormQuery(java.lang.String  freeFormQueryString) throws java.rmi.RemoteException {
            System.out.println("+++++++++THIS IS FREE FORM QUERY++++++++++++");
            IFetaModel fetta = getStore();

            try {
                    return fetta.freeFormQuery(freeFormQueryString);

            } catch (FetaEngineException e) {
                    e.printStackTrace();
                    throw new RemoteException("problem during query execution in Feta engine", e);
            }
	}


	public uk.ac.man.cs.img.fetaEngine.webservice.FetaSearchResponseType copyToArray(Set resultSet) {

            Iterator i = resultSet.iterator();
            int size = resultSet.size();
            int ind = 0;
            if (size>0) {
                    String[] resultArray = new String[size];
                    while (i.hasNext())
                     {
                            resultArray[ind] = ((String) i.next());
                            ind++;

                     }
                    return new FetaSearchResponseType(resultArray);
            }
            else {//no results
                   // return and empty response..
                    return new FetaSearchResponseType();
            }

	}


	public uk.ac.man.cs.img.fetaEngine.webservice.FetaPublishResponseType publishDescription(java.lang.String  operationURI) throws java.rmi.RemoteException {
      
          System.out.println("+++++++++THIS IS PUBLISH++++++++++++");
          IFetaModel fetta = getStore();
          FetaPublishResponseType response = new FetaPublishResponseType();
          response.setPublishResult(PublishResultType.Success);
          response.setPublishMessage("Publish is successful");

          
          if (operationURI.endsWith(".rdf")){
             try{
                fetta.publishDescription(operationURI);
                return response;
                
             }catch (Exception ex){
                ex.printStackTrace();                
                response.setPublishResult(PublishResultType.Failure);
                response.setPublishMessage( ex.getMessage());
                return response;                
             }
    
          } else if (operationURI.endsWith(".xml")){
                try{
                       PedroXMLToRDF rdfConverter = new PedroXMLToRDF();   
                       List locList = new ArrayList();
                       locList.add(operationURI);
                       URL operationURL  = new URL(operationURI);
                       Map docAsRDF = new HashMap(); 
                       docAsRDF = new FetaLoad().readFetaDescriptions(locList,'u');
                       String RDFXMLStr = rdfConverter.convertToRdfXml((Document)docAsRDF.get(operationURL.toString()));
                       fetta.publishDescription(operationURI, RDFXMLStr);
                       return response;                      
                       
                }catch (Exception exp){
                    exp.printStackTrace();
                    response.setPublishResult(PublishResultType.Failure);
                    response.setPublishMessage("Problem when converting XML desc to RDF"+ exp.getMessage());                    
                    return response;                    
                }

       
          } else {  
                    response.setPublishResult(PublishResultType.Failure);
                    response.setPublishMessage("unsupported format");                       
                    return response;
          }
          
          
     }
          
	public uk.ac.man.cs.img.fetaEngine.webservice.FetaRemoveResponseType removeDescription(java.lang.String  operationURLString) throws java.rmi.RemoteException {

            System.out.println("+++++++++THIS IS REMOVE++++++++++++");
            IFetaModel fetta = getStore();
            
            FetaRemoveResponseType response = new FetaRemoveResponseType();
            response.setRemoveResult(PublishResultType.Success);
            response.setRemoveMessage("Remove is successful");
            
            try {
                    fetta.removeDescription(operationURLString);
                    return response;
                    

            } catch (FetaEngineException e) {
                    e.printStackTrace();
                    response.setRemoveResult(PublishResultType.Failure);
                    response.setRemoveMessage(e.getMessage());                    
                    return response;
                    
            } catch(Exception exp){
                    exp.printStackTrace();
                    response.setRemoveResult(PublishResultType.Failure);
                    response.setRemoveMessage(exp.getMessage());   
                    return response;                                        
             }                        
	}


	public IFetaModel getStore() throws RemoteException {
	   return feta;
	}


	 private void appendServiceSet(Set resultSet,Set cannedQueryResults, boolean prelim) {
            if (prelim==true) resultSet.addAll(cannedQueryResults);
            else  resultSet.retainAll(cannedQueryResults);

	}

}


