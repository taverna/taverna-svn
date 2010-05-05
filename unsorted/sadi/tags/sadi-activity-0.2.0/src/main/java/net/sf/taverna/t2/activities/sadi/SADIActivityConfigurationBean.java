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
package net.sf.taverna.t2.activities.sadi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration bean for a {@link SADIActivity}.
 * 
 * @author David Withers
 */
public class SADIActivityConfigurationBean {

	private String sparqlEndpoint, graphName, serviceURI;

	private List<List<String>> inputRestrictionPaths, outputRestrictionPaths;
	
	private Map<String, String> attributes;

	public SADIActivityConfigurationBean() {
		inputRestrictionPaths = new ArrayList<List<String>>();
		outputRestrictionPaths = new ArrayList<List<String>>();
		attributes = new HashMap<String, String>();
	}

	public SADIActivityConfigurationBean(SADIActivityConfigurationBean configuration) {
		sparqlEndpoint = configuration.getSparqlEndpoint();
		graphName = configuration.getGraphName();
		serviceURI = configuration.getServiceURI();
		inputRestrictionPaths = configuration.getInputRestrictionPaths();
		outputRestrictionPaths = configuration.getOutputRestrictionPaths();
		attributes = configuration.getAttributes();
	}

	private List<List<String>> copyPaths(List<List<String>> paths) {
		List<List<String>> copy = new ArrayList<List<String>>();
		for (List<String> path : paths) {
			List<String> newPath = new ArrayList<String>();
			newPath.addAll(path);
			copy.add(newPath);
		}
		return copy;
	}

	/**
	 * Returns the sparqlEndpoint.
	 * 
	 * @return the sparqlEndpoint
	 */
	public String getSparqlEndpoint() {
		return sparqlEndpoint;
	}

	/**
	 * Sets the value of sparqlEndpoint.
	 * 
	 * @param sparqlEndpoint
	 *            the new value for sparqlEndpoint
	 */
	public void setSparqlEndpoint(String sparqlEndpoint) {
		this.sparqlEndpoint = sparqlEndpoint;
	}

	/**
	 * Returns the graphName.
	 * 
	 * @return the graphName
	 */
	public String getGraphName() {
		return graphName;
	}

	/**
	 * Sets the value of graphName.
	 * 
	 * @param graphName
	 *            the new value for graphName
	 */
	public void setGraphName(String graphName) {
		this.graphName = graphName;
	}

	/**
	 * Returns the service URI.
	 * 
	 * @return the service URI
	 */
	public String getServiceURI() {
		return serviceURI;
	}

	/**
	 * Sets the value of service URI.
	 * 
	 * @param serviceURI
	 *            the new value for service URI
	 */
	public void setServiceURI(String serviceURI) {
		this.serviceURI = serviceURI;
	}

	/**
	 * Returns the paths that specify the property restrictions that will be
	 * inputs to the {@link SADIActivity}.
	 * 
	 * @return the paths that specify the property restrictions that will be
	 *         inputs to the {@link SADIActivity}
	 */
	public List<List<String>> getInputRestrictionPaths() {
		return copyPaths(inputRestrictionPaths);
	}

	/**
	 * Sets the paths that specify the property restrictions that will be inputs
	 * to the {@link SADIActivity}.
	 * 
	 * @param inputRestrictionPaths
	 *            the paths that specify the property restrictions that will be
	 *            inputs to the {@link SADIActivity}.
	 */
	public void setInputRestrictionPaths(List<List<String>> inputRestrictionPaths) {
		if (inputRestrictionPaths == null) {
			this.inputRestrictionPaths.clear();
		} else {
			this.inputRestrictionPaths = copyPaths(inputRestrictionPaths);
		}
	}

	/**
	 * Returns the paths that specify the property restrictions that will be
	 * outputs from the {@link SADIActivity}.
	 * 
	 * @return the paths that specify the property restrictions that will be
	 *         outputs from the {@link SADIActivity}
	 */
	public List<List<String>> getOutputRestrictionPaths() {
		return copyPaths(outputRestrictionPaths);
	}

	/**
	 * Sets the paths that specify the property restrictions that will be
	 * outputs from the {@link SADIActivity}.
	 * 
	 * @param outputRestrictionPaths
	 *            the paths that specify the property restrictions that will be
	 *            outputs from the {@link SADIActivity}
	 */
	public void setOutputRestrictionPaths(List<List<String>> outputRestrictionPaths) {
		if (outputRestrictionPaths == null) {
			this.outputRestrictionPaths.clear();
		} else {
			this.outputRestrictionPaths = copyPaths(outputRestrictionPaths);
		}
	}

	public void addInputRestrictionPath(List<String> restrictionPath) {
		inputRestrictionPaths.add(restrictionPath);
	}

	public void removeInputRestrictionPath(List<String> restrictionPath) {
		inputRestrictionPaths.remove(restrictionPath);
	}

	public void addOutputRestrictionPath(List<String> restrictionPath) {
		outputRestrictionPaths.add(restrictionPath);
	}

	public void removeOutputRestrictionPath(List<String> restrictionPath) {
		outputRestrictionPaths.remove(restrictionPath);
	}

	public String getAttribute(String name) {
		return attributes.get(name);
	}
	
	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}
	
	public void removeAttribute(String name) {
		attributes.remove(name);
	}
	
	public Map<String, String> getAttributes() {
		return new HashMap<String, String>(attributes);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((graphName == null) ? 0 : graphName.hashCode());
		result = prime * result
				+ ((inputRestrictionPaths == null) ? 0 : inputRestrictionPaths.hashCode());
		result = prime * result
				+ ((outputRestrictionPaths == null) ? 0 : outputRestrictionPaths.hashCode());
		result = prime * result + ((serviceURI == null) ? 0 : serviceURI.hashCode());
		result = prime * result + ((sparqlEndpoint == null) ? 0 : sparqlEndpoint.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SADIActivityConfigurationBean other = (SADIActivityConfigurationBean) obj;
		if (attributes == null) {
			if (other.attributes != null) {
				return false;
			}
		} else if (!attributes.equals(other.attributes)) {
			return false;
		}
		if (graphName == null) {
			if (other.graphName != null) {
				return false;
			}
		} else if (!graphName.equals(other.graphName)) {
			return false;
		}
		if (inputRestrictionPaths == null) {
			if (other.inputRestrictionPaths != null) {
				return false;
			}
		} else if (!inputRestrictionPaths.equals(other.inputRestrictionPaths)) {
			return false;
		}
		if (outputRestrictionPaths == null) {
			if (other.outputRestrictionPaths != null) {
				return false;
			}
		} else if (!outputRestrictionPaths.equals(other.outputRestrictionPaths)) {
			return false;
		}
		if (serviceURI == null) {
			if (other.serviceURI != null) {
				return false;
			}
		} else if (!serviceURI.equals(other.serviceURI)) {
			return false;
		}
		if (sparqlEndpoint == null) {
			if (other.sparqlEndpoint != null) {
				return false;
			}
		} else if (!sparqlEndpoint.equals(other.sparqlEndpoint)) {
			return false;
		}
		return true;
	}

}
