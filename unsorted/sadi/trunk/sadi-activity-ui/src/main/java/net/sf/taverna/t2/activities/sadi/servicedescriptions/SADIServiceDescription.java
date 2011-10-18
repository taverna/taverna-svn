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

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.activities.sadi.SADIActivity;
import net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;

/**
 * A {@link ServiceDescription} for the {@link SADIActivity}.
 *
 * @author David Withers
 */
public class SADIServiceDescription extends ServiceDescription<SADIActivityConfigurationBean>{

	private String sparqlEndpoint, graphName;
	
	private ca.wilkinsonlab.sadi.ServiceDescription serviceInfo;
			
	private static final String SERVICE = "SADI @ ";
	
	/**
	 * Returns the source registry's SPARQL endpoint.
	 *
	 * @return the source registry's SPARQL endpoint
	 */
	public String getSparqlEndpoint() {
		return sparqlEndpoint;
	}

	/**
	 * Sets the value of sparqlEndpoint.
	 *
	 * @param sparqlEndpoint the new value for sparqlEndpoint
	 */
	public void setSparqlEndpoint(String sparqlEndpoint) {
		this.sparqlEndpoint = sparqlEndpoint;
	}

	/**
	 * Returns the source registry's SPARQL graph name.
	 *
	 * @return the source registry's SPARQL graph name
	 */
	public String getGraphName() {
		return graphName;
	}

	/**
	 * Sets the value of graphName.
	 *
	 * @param graphName the new value for graphName
	 */
	public void setGraphName(String graphName) {
		this.graphName = graphName;
	}

	/**
	 * Returns the SADI service description.
	 *
	 * @return the SADI service description
	 */
	public ca.wilkinsonlab.sadi.ServiceDescription getServiceInfo() {
		return serviceInfo;
	}

	/**
	 * Set the SADI service description.
	 * 
	 * @param serviceInfo the SADI service description
	 */
	public void setServiceInfo(ca.wilkinsonlab.sadi.ServiceDescription serviceInfo) {
		this.serviceInfo = serviceInfo;
	}

	@Override
	public Class<SADIActivity> getActivityClass() {
		return SADIActivity.class;
	}

	@Override
	public SADIActivityConfigurationBean getActivityConfiguration() {
		SADIActivityConfigurationBean configuration = new SADIActivityConfigurationBean();
		configuration.setSparqlEndpoint(sparqlEndpoint);
		configuration.setGraphName(graphName);
		configuration.setServiceURI(serviceInfo == null ? null : serviceInfo.getURI());
		/* TODO add default ports here? we have the ServiceDescription, 
		 * which should have all of the information we need...
		 */
		return configuration;
	}

	@Override
	public Icon getIcon() {
		return SADIActivityIcon.getSADIIcon();
	}

	@Override
	public String getName() {
		return serviceInfo == null ? null : serviceInfo.getName();
	}
	
	@Override
	public String getDescription() {
		return serviceInfo == null ? null : serviceInfo.getDescription();
	}
	
	@Override
	public List<? extends Comparable<?>> getPath() {
		return Arrays.asList(SERVICE + getSparqlEndpoint());
	}

	@Override
	protected List<Object> getIdentifyingData() {
		return Arrays.<Object>asList(
				sparqlEndpoint, 
				graphName, 
				serviceInfo == null ? null : serviceInfo.getURI());
	}
	
	@Override
	public boolean isTemplateService() {
		return false;
	}

}
