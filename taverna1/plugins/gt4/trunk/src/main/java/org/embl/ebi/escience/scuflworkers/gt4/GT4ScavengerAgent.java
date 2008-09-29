
/*
 * Copyright (C) 2003 The University of Chicago 
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
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: GT4ScavengerAgent.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008-05-14 07:14:48 $
 *               by   $Author: tanwei $
 * Created on 01-Dec-2007
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.gt4;

//Comments: do not use CaGrid client API, use pure WS client instead

import gov.nih.nci.cagrid.discovery.client.DiscoveryClient;
import gov.nih.nci.cagrid.metadata.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;
import gov.nih.nci.cagrid.metadata.ServiceMetadataServiceDescription;
import gov.nih.nci.cagrid.metadata.common.PointOfContact;
import gov.nih.nci.cagrid.metadata.common.UMLClass;
import gov.nih.nci.cagrid.metadata.exceptions.QueryInvalidException;
import gov.nih.nci.cagrid.metadata.exceptions.RemoteResourcePropertyRetrievalException;
import gov.nih.nci.cagrid.metadata.exceptions.ResourcePropertyRetrievalException;
import gov.nih.nci.cagrid.metadata.service.Operation;
//import gov.nih.nci.cagrid.metadata.service.OperationInputParameterCollection;
import gov.nih.nci.cagrid.metadata.service.ServiceContext;
import gov.nih.nci.cagrid.metadata.service.ServiceServiceContextCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;

/**
 * An agent to query gt4 server to determine the available categories and services.
 * @author sowen
 *
 */

public class GT4ScavengerAgent {
	
	private static Logger logger = Logger.getLogger(GT4ScavengerAgent.class);
		
	/**
	 * Returns a list of GT4 services, containing a list of their operations.
	 * Throws Exception if a service cannot be found.
	 */	
	public static List<GT4Service> load(String indexURL, ServiceQuery[] sq) throws Exception{
		List<GT4Service> services=new ArrayList<GT4Service>();
		
		// Get the categories for this installation
		boolean findServices = loadServices(indexURL,sq,services);
		if (!findServices) {
			
			throw new Exception("Unable to locate a GT4 index at \n" + indexURL);
		}
		
		return services;
		
	}
	
	//load services & operations by caGrid discovery service API
	private static boolean loadServices(String indexURL, ServiceQuery[] sq, List<GT4Service>services) throws Exception{
		boolean foundSome = false;
		
		System.out.println("==================================================");
		System.out.println("Start to generate Scavenger");		
		EndpointReferenceType[] servicesList = null;
		servicesList = getEPRListByServiceQueryArray(indexURL,sq);
	        System.out.println("DiscoveryClient loaded and EPR to services returned.");
	        for (EndpointReferenceType epr : servicesList) {
	        	if(epr!=null){
	        		foundSome = true;
		        	//add a service node
		        	String serviceAddress = epr.getAddress().toString();
		            GT4Service s = new GT4Service(serviceAddress+"?wsdl",serviceAddress);
		            services.add(s);
		            System.out.println(serviceAddress+"?wsdl");
		            try{
		            	ServiceMetadata serviceMetadata = MetadataUtils.getServiceMetadata(epr);
		            	ServiceMetadataServiceDescription serviceDes = serviceMetadata.getServiceDescription();
		            	
		            	//ServiceContextOperationCollection s = 
		            		//serviceDes.getService().getServiceContextCollection().getServiceContext(0).getOperationCollection();
		            	
		            	ServiceServiceContextCollection srvContxCol = serviceDes.getService().getServiceContextCollection();
		            	ServiceContext [] srvContxs  =srvContxCol.getServiceContext();
		            	for (ServiceContext srvcontx:srvContxs)
		            	{
		            		Operation [] ops = srvcontx.getOperationCollection().getOperation();
		            		
		            		//TODO: portType is no longer needed??
		            		for (Operation op :ops){
		            			//add an operation node
		            			//print out the name of an operation
		            			String operationName = op.getName();
		            			//OperationInputParameterCollection opp = op.getInputParameterCollection();
		            			
		            			s.addOperation(operationName);
		            			System.out.println(operationName);	            			
		            		}
		            	}
		            }
		            	
		            catch (Exception e)
		            {
		            	e.printStackTrace();           	
		            }
	        	}
	        	

	        }
		return foundSome;
	}	
		
	public static EndpointReferenceType[] getEPRListByServiceQuery(String indexURL, ServiceQuery sq){
		EndpointReferenceType[] servicesList = null;
		DiscoveryClient client = null;
		try {
			client = new DiscoveryClient(indexURL);
		} catch (MalformedURIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(sq==null){			
			System.out.println("Retrieving all services from the index: "+ indexURL);
			System.out.println("==================================================");			
			try {
				servicesList = client.getAllServices(true);
			} catch (RemoteResourcePropertyRetrievalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (QueryInvalidException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ResourcePropertyRetrievalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {		
			
			System.out.println("Service Index URL: "+indexURL);
			//semanticQueryingClause = indexURL.substring(n1+2);
			
			System.out.println("Service Query: " + sq.queryCriteria + "  == "+ sq.queryValue);
		    System.out.println("==================================================");
			  //TODO: semantic based service searching
			  //query by Search String
			 if(sq.queryCriteria.equals("Search String")){
				  try {
					servicesList = client.discoverServicesBySearchString(sq.queryCriteria);
				} catch (RemoteResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			  
			  }
			  //query by Research Center Name
			  else if(sq.queryCriteria.equals("Research Center")){
				  try {
					servicesList = client.discoverServicesByResearchCenter(sq.queryValue);
				} catch (RemoteResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			  
			  }
			//query by Point of Contact
			  else if(sq.queryCriteria.equals("Point Of Contact")){
				  PointOfContact poc = new PointOfContact();
				  int n3 = sq.queryValue.indexOf(" ");
				  String firstName = sq.queryValue.substring(0,n3);
				  String lastName = sq.queryValue.substring(n3+1);
			      poc.setFirstName(firstName);
			      poc.setLastName(lastName);
				  try {
					servicesList = client.discoverServicesByPointOfContact(poc);
				} catch (RemoteResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			  
			  }
			//query by Service Name
			  else if(sq.queryCriteria.equals("Service Name")){
				  try {
					servicesList = client.discoverServicesByName(sq.queryValue);
				} catch (RemoteResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		  
			  }
			//query by Operation Name
			  else if(sq.queryCriteria.equals("Operation Name")){
				  try {
					servicesList = client.discoverServicesByOperationName(sq.queryValue);
				} catch (RemoteResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		  
			  }
			//query by Operation Input
			  else if(sq.queryCriteria.equals("Operation Input")){
				  UMLClass umlClass = new UMLClass();
			        umlClass.setClassName(sq.queryValue);
				  try {
					servicesList = client.discoverServicesByOperationInput(umlClass);
				} catch (RemoteResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		  
			  }
			//query by Operation Output
			  else if(sq.queryCriteria.equals("Operation Output")){
				  UMLClass umlClass = new UMLClass();
			        umlClass.setClassName(sq.queryValue);
				  try {
					servicesList = client.discoverServicesByOperationOutput(umlClass);
				} catch (RemoteResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		  
			  }
			//query by Operation Class
			  else if(sq.queryCriteria.equals("Operation Class")){
				  UMLClass umlClass = new UMLClass();
			        umlClass.setClassName(sq.queryValue);
				  try {
					servicesList = client.discoverServicesByOperationClass(umlClass);
				} catch (RemoteResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		  
			  }
			 //discoverServicesByConceptCode("C43418")
			  else if(sq.queryCriteria.equals("Concept Code")){
				  try {
					servicesList = client.discoverServicesByConceptCode(sq.queryValue);
				} catch (RemoteResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		  
			  }
			 //discoverServicesByOperationConceptCode
			 //discoverServicesByDataConceptCode
			 //discoverServicesByPermissibleValue
			 //getAllDataServices
			 //discoverDataServicesByDomainModel("caCore")
			  else if(sq.queryCriteria.equals("Domain Model for Data Services")){
				  try {
					servicesList = client.discoverDataServicesByDomainModel(sq.queryValue);
				} catch (RemoteResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		  
			  }
			 //discoverDataServicesByModelConceptCode
			 //discoverDataServicesByExposedClass
			 //discoverDataServicesByPermissibleValue
			 //discoverDataServicesByAssociationsWithClass
			 //discoverByFilter
		}
		return servicesList;
		
}
	public static EndpointReferenceType[] getEPRListByServiceQueryArray(String indexURL, ServiceQuery sq[]){
		EndpointReferenceType[] servicesList = null;
		if(sq==null){
			return getEPRListByServiceQuery(indexURL, null); 
		}
		else if(sq.length==1){
			return getEPRListByServiceQuery(indexURL, sq[0]); 
		}
		//sq holds more than 1 queries		
		else if(sq.length>1){
			EndpointReferenceType[][] tempEPRList = new EndpointReferenceType[sq.length][];
			for(int i=0;i<sq.length;i++){
				tempEPRList[i] = getEPRListByServiceQuery(indexURL,sq[i]);	
			}
			return CombineEPRList(tempEPRList);
		}
		return servicesList;
		
		
	}
	public static EndpointReferenceType[] CombineEPRList(EndpointReferenceType[][] tempEPRList){
		EndpointReferenceType[] servicesList = null;	
		String [][] addressList = new String [tempEPRList.length][];
		for (int i=0;i<tempEPRList.length;i++){
			addressList[i] = new String [tempEPRList[i].length];
			for (int j=0;j<tempEPRList[i].length;j++){
				addressList[i][j] = tempEPRList[i][j].getAddress().toString(); 
			}
		}
		List alist = new ArrayList(Arrays.asList(addressList[0]));
		for (int i=1;i<tempEPRList.length;i++){
			alist.retainAll(Arrays.asList(addressList[i]));
		}
		
		int count = 0;
		int [] flag = new int [tempEPRList[0].length];
		for (int i=0;i<tempEPRList[0].length;i++){
			if(alist.contains(tempEPRList[0][i].getAddress().toString())){
				count ++ ;
				flag[i]=1;
			}
		}
		servicesList = new EndpointReferenceType[count];
		int j=0;
		for (int i=0;i<tempEPRList[0].length;i++){
			
			if(flag[i]==1){
				servicesList [j++] = tempEPRList[0][i];
			}
		}
		return servicesList;
	}

	
}
class ServiceMetaData{
	String [] serviceAddress;
	String [][]operationName;
	ServiceMetaData(){
		String [] serviceAddress = null;
		String [][]operationName = null;
	}
	
	
}
