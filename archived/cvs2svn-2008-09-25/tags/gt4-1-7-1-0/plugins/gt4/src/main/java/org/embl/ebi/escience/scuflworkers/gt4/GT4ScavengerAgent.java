
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
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008-01-05 14:54:32 $
 *               by   $Author: tanwei $
 * Created on 01-Dec-2007
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.gt4;

//Comments: do not use CaGrid client API, use pure WS client instead
/*
import gov.nih.nci.cagrid.discovery.client.DiscoveryClient;
import gov.nih.nci.cagrid.metadata.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;
import gov.nih.nci.cagrid.metadata.ServiceMetadataServiceDescription;
import gov.nih.nci.cagrid.metadata.service.Operation;
//import gov.nih.nci.cagrid.metadata.service.OperationInputParameterCollection;
import gov.nih.nci.cagrid.metadata.service.ServiceContext;
import gov.nih.nci.cagrid.metadata.service.ServiceServiceContextCollection;
*/
import java.util.ArrayList;
import java.util.List;

//import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.log4j.Logger;

/**
 * An agent to query gt4 server to determine the available categories and services.
 * @author sowen
 *
 */

public class GT4ScavengerAgent {
	
	private static Logger logger = Logger.getLogger(GT4ScavengerAgent.class);
		
	/**
	 * Returns a list of soaplab categories, containing a list of their services.
	 * Throws MissingSoaplabException if an installation cannot be found.
	 */	
	public static List<GT4Service> load(String indexURL) throws Exception{
		List<GT4Service> services=new ArrayList<GT4Service>();
		
		// Get the categories for this installation
		boolean findAnIndex = loadServices(indexURL,services);
		if (!findAnIndex) {
			
			throw new Exception("Unable to locate a GT4 index at \n" + indexURL);
		}
		
		return services;
		
	}
	
	/*//load services & operations by caGrid discovery service API
	private static boolean loadServices(String indexURL, List<GT4Service>services) throws Exception{
		boolean foundSome = true;
		//TODO load service metadata from index service
		System.out.println("Start to generate DiscoveryClient");
		  DiscoveryClient client = new DiscoveryClient(indexURL);
	        EndpointReferenceType[] allServices = client.getAllServices(true);
	        System.out.println("DiscoveryClient loaded and EPR to services returned.");
	        for (EndpointReferenceType epr : allServices) {
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
		return foundSome;
	}	
	
	*/
	private static boolean loadServices(String indexURL, List<GT4Service>services) 
	throws Exception{
		ServiceMetaData serviceMetaData = new ServiceMetaData();
		IndexService.getServiceMetaData(indexURL, serviceMetaData);
		for (int i=0;i<serviceMetaData.serviceAddress.length;i++) {
        	//add a service node
            GT4Service s = new GT4Service(serviceMetaData.serviceAddress[i]+"?wsdl",serviceMetaData.serviceAddress[i]);
            services.add(s);
            System.out.println(serviceMetaData.serviceAddress[i]+"?wsdl");
            
        	for (int j=0;j<serviceMetaData.operationName[i].length;j++)
        	{
        		s.addOperation(serviceMetaData.operationName[i][j]);
        	}
            		
        }
		return true;
	}
	
	/*
	//TODO: modify this function to access real index service
	private static boolean loadServices(String indexURL, List<GT4Service>services) 
	throws Exception{
		String address1 = "http://cagrid-service.nci.nih.gov:8080/wsrf/services/cagrid/CaDSRService?wsdl";
		GT4Service s1 = new GT4Service(address1,address1);
        services.add(s1);
        s1.addOperation("findAllProjects");
        s1.addOperation("findProjects");
        s1.addOperation("findPackagesInProject");
        s1.addOperation("findAssociationsInProject");
        s1.addOperation("findContextForProject");
        
        String address2 = "http://cagrid-service.nci.nih.gov:8080/wsrf/services/cagrid/EVSGridService?wsdl";
		GT4Service s2 = new GT4Service(address2,address2);
        services.add(s2);
        s2.addOperation("getVocabularyNames");
        s2.addOperation("getMetaSources");	
		return true;
	}
	*/
		
}
class ServiceMetaData{
	String [] serviceAddress;
	String [][]operationName;
	ServiceMetaData(){
		String [] serviceAddress = null;
		String [][]operationName = null;
	}
	
	
}
