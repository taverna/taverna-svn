/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ca.wilkinsonlab.sadi.client.Registry;
import ca.wilkinsonlab.sadi.client.RegistryImpl;
import ca.wilkinsonlab.sadi.utils.QueryExecutorFactory;

/**
 * A cache of SADI registries.
 * 
 * @author David Withers
 */
public class SADIRegistries {

	private static final Map<RegistryDetails, Registry> registries = new HashMap<RegistryDetails, Registry>();

	private SADIRegistries() {
	}

	/**
	 * Returns the SADI Registry that has the specified sparql endpoint and
	 * graph name. If the registry doesn't exist it will be created.
	 * 
	 * @param sparqlEndpoint
	 *            the URL of the registry sparql endpoint
	 * @param graphName
	 *            the URL of the registry graph name
	 * @return the SADI Registry that has the specified sparql endpoint and graph name
	 * @throws IOException
	 *             it the registry cannot be created
	 */
	public static Registry getRegistry(String sparqlEndpoint, String graphName) throws IOException {
		RegistryDetails rd = new RegistryDetails(sparqlEndpoint, graphName);
		synchronized (registries) {
			if (!registries.containsKey(rd)) {
				Registry registry = new RegistryImpl(QueryExecutorFactory
						.createVirtuosoSPARQLEndpointQueryExecutor(sparqlEndpoint, graphName));
				registries.put(rd, registry);
				return registry;
			}
			return registries.get(rd);
		}
	}

	/**
	 * Returns an unmodifiable collection of the registries in the cache.
	 * 
	 * @return an unmodifiable collection of the registries in the cache
	 */
	public static Collection<Registry> getRegistries() {
		synchronized (registries) {
			return Collections.unmodifiableCollection(registries.values());
		}
	}

	/**
	 * Returns an unmodifiable map of the registries in the cache.
	 * 
	 * @return an unmodifiable map of the registries in the cache
	 */
	public static Map<RegistryDetails, Registry> getRegistryMap() {
		synchronized (registries) {
			return Collections.unmodifiableMap(registries);
		}
	}

	/**
	 * Removes all registries from the cache.
	 */
	public static void clear() {
		synchronized (registries) {
			registries.clear();
		}
	}
	
	/**
	 * Wrapper for the sparql endpoint and graph name of a SADI Registry.
	 *
	 * @author David Withers
	 */
	public static class RegistryDetails {

		private String sparqlEndpoint, graphName;

		/**
		 * Constructs a new RegistryDetails.
		 * 
		 * @param sparqlEndpoint
		 * @param graphName
		 */
		public RegistryDetails(String sparqlEndpoint, String graphName) {
			this.sparqlEndpoint = sparqlEndpoint;
			this.graphName = graphName;
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
		 * Returns the graphName.
		 * 
		 * @return the graphName
		 */
		public String getGraphName() {
			return graphName;
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
			RegistryDetails other = (RegistryDetails) obj;
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

}
