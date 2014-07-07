/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.sadi.servicedescriptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean;
import net.sf.taverna.t2.activities.sadi.SADIRegistries;
import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import ca.wilkinsonlab.sadi.SADIException;
import ca.wilkinsonlab.sadi.client.RegistryImpl;
import ca.wilkinsonlab.sadi.client.Service;

/**
 * 
 *
 * @author David Withers
 */
public class SADIServiceProvider extends AbstractConfigurableServiceProvider<SADIServiceProviderConfig> {

	private static final String SERVICE_NAME = "SADI";
	
	private static final int partialResultSize = 50;
	
	private static Logger logger = Logger.getLogger(SADIServiceProvider.class);

	public SADIServiceProvider() {
		super(new SADIServiceProviderConfig("http://somehost/sparql", "http://somehost/registry"));
	}

	public void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack) {
		List<ServiceDescription<SADIActivityConfigurationBean>> descriptions = new ArrayList<ServiceDescription<SADIActivityConfigurationBean>>();
		String sparqlEndpoint = serviceProviderConfig.getSparqlEndpoint();
		String graphName = serviceProviderConfig.getGraphName();
		callBack.status("About to find services for " + SERVICE_NAME + " at " + sparqlEndpoint);		
		try {
			int offset=0, servicesReturned=0;
			do {
				/* FIXME stop using the implementation directly when this method
				 * is moved to the interface...
				 */
				Collection<Service> services = ((RegistryImpl)SADIRegistries.getRegistry(sparqlEndpoint, graphName)).getAllServices(partialResultSize, offset);
				servicesReturned = services.size();
				offset += servicesReturned;
				for (Service service : services) {
					try {
						ServiceDescription<SADIActivityConfigurationBean> serviceDescription = getServiceDescription(service);
						descriptions.add(serviceDescription);
					}
					catch (Exception e) {
						logger.error("Service description creation failed for " + service.getURI(), e);
					}
				}
				callBack.partialResults(descriptions);
				descriptions.clear();
			} while (servicesReturned == partialResultSize);
			callBack.finished();
		}
		catch (IOException e) {
			callBack.fail("Failed to find services for " + SERVICE_NAME + " at " + sparqlEndpoint, e);
		} catch (SADIException e) {
			callBack.fail("Failed to find services for " + SERVICE_NAME + " at " + sparqlEndpoint, e);
		}
	}

	public Icon getIcon() {
		return SADIActivityIcon.getSADIIcon();
	}

	public String getName() {
		return SERVICE_NAME;
	}

	@Override
	public List<SADIServiceProviderConfig> getDefaultConfigurations() {
		/* TODO 
		 * it would be nice if this used ca.wilkinsonlab.sadi.client.Config.getRegistries();
		 * can we update it so it does? is this method only called the first time the plugin
		 * is used or will returning clients get the updated defaults?
		 */
		List<SADIServiceProviderConfig> defaults = new ArrayList<SADIServiceProviderConfig>();
		defaults.add(new SADIServiceProviderConfig("http://biordf.net/sparql", "http://sadiframework.org/registry/"));
		return defaults;
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		List<String> result;
		result = Arrays.asList(getConfiguration().getSparqlEndpoint());
		return result;
	}

	public String getId() {
		return "http://sadiframework.org/registry/serviceprovider";
	}
	
	private SADIServiceDescription getServiceDescription(ca.wilkinsonlab.sadi.ServiceDescription service)
	{
		SADIServiceDescription item = new SADIServiceDescription();
		item.setSparqlEndpoint(serviceProviderConfig.getSparqlEndpoint());
		item.setGraphName(serviceProviderConfig.getGraphName());
		item.setServiceInfo(service);
		return item;
	}
	
}

