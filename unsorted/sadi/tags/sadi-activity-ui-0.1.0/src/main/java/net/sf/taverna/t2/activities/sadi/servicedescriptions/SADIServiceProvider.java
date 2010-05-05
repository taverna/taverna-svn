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
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean;
import net.sf.taverna.t2.activities.sadi.SADIRegistries;
import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import ca.wilkinsonlab.sadi.client.Service;
import ca.wilkinsonlab.sadi.common.SADIException;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * 
 *
 * @author David Withers
 */
public class SADIServiceProvider extends AbstractConfigurableServiceProvider<SADIServiceProviderConfig> {

	private static final String SERVICE_NAME = "SADI";
	
	private static final String lsrnURI = "http://purl.oclc.org/SADI/LSRN/LSRN_Record";
	
	private static final Resource lsrnResource = ResourceFactory.createResource(lsrnURI);

	private static final int partialResultSize = 5;

	private static ServiceDescriptionRegistry serviceDescriptionRegistry;
	
	public SADIServiceProvider() {
		super(new SADIServiceProviderConfig("http://somehost/sparql", "http://somehost/registry"));
		OntDocumentManager.getInstance().setCacheModels(true);
	}

	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		List<ServiceDescription<SADIActivityConfigurationBean>> descriptions = new ArrayList<ServiceDescription<SADIActivityConfigurationBean>>();
		String sparqlEndpoint = serviceProviderConfig.getSparqlEndpoint();
		String graphName = serviceProviderConfig.getGraphName();
		callBack.status("About to find services for " + SERVICE_NAME + " at " + sparqlEndpoint);		
		try {
			for (Service service : SADIRegistries.getRegistry(sparqlEndpoint, graphName).getAllServices()) {
				try {
					OntClass inputClass = service.getInputClass();
					if (inputClass != null && inputClass.hasSuperClass(lsrnResource)) {
						SADIServiceDescription item = new SADIServiceDescription();
						item.setSparqlEndpoint(sparqlEndpoint);
						item.setGraphName(graphName);
						item.setServiceURI(service.getServiceURI());
						item.setName(service.getName());
						item.setDescription(service.getDescription());
						descriptions.add(item);
						if (descriptions.size() == partialResultSize) {
							callBack.partialResults(descriptions);
							descriptions.clear();
						}
					}
				} catch (SADIException e) {
					callBack.fail("Error while fetching input class for " + service.getName(), e);
				}
			}
			if (descriptions.size() > 0) {
				callBack.partialResults(descriptions);
			}
			callBack.finished();
		}
		catch (IOException e) {
			callBack.fail("Failed to find services for " + SERVICE_NAME + " at " + sparqlEndpoint, e);
		}
	}

	public Icon getIcon() {
		return SADIActivityIcon.getSADIIcon();
	}

	public String getName() {
		return SERVICE_NAME;
	}

	public List<SADIServiceProviderConfig> getDefaultConfigurations() {
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

    /**
     * Will be set by ServiceDescriptionRegistryImpl.
     * TODO Doesn't seem to get set. Why not?
     *
     * @param registry Registry this provider has been added to
     */
    public void setServiceDescriptionRegistry(ServiceDescriptionRegistry registry) {
    	serviceDescriptionRegistry = registry;
    }

	/**
	 * Returns the serviceDescriptionRegistry.
	 *
	 * @return the serviceDescriptionRegistry
	 */
	public static ServiceDescriptionRegistry getServiceDescriptionRegistry() {
		return serviceDescriptionRegistry;
	}

	
}

