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

import net.sf.taverna.t2.lang.beans.PropertyAnnotated;
import net.sf.taverna.t2.lang.beans.PropertyAnnotation;

/**
 * Configuration for a {@link SADIServiceProvider}.
 *
 * @author David Withers
 */
public class SADIServiceProviderConfig extends PropertyAnnotated {

	private String sparqlEndpoint, graphName;
	
	public SADIServiceProviderConfig() {
	}

	/**
	 * Constructs a new SADIServiceProviderConfig.
	 * 
	 * @param sparqlEndpoint
	 * @param graphName
	 */
	public SADIServiceProviderConfig(String sparqlEndpoint, String graphName) {
		this.sparqlEndpoint = sparqlEndpoint;
		this.graphName = graphName;
	}


	/**
	 * Returns the sparqlEndpoint.
	 *
	 * @return the sparqlEndpoint
	 */
	@PropertyAnnotation(displayName = "SADI sparql endpoint", preferred = true)
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
	 * Returns the graphName.
	 *
	 * @return the graphName
	 */
	@PropertyAnnotation(displayName = "SADI graph name")
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

	public String toString() {
		return getSparqlEndpoint();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((graphName == null) ? 0 : graphName.hashCode());
		result = prime * result + ((sparqlEndpoint == null) ? 0 : sparqlEndpoint.hashCode());
		return result;
	}

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
		SADIServiceProviderConfig other = (SADIServiceProviderConfig) obj;
		if (graphName == null) {
			if (other.graphName != null) {
				return false;
			}
		} else if (!graphName.equals(other.graphName)) {
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
