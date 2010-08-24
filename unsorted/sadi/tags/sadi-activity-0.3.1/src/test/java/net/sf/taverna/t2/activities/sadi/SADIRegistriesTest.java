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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import net.sf.taverna.t2.activities.sadi.SADIRegistries.RegistryDetails;

import org.junit.Before;
import org.junit.Test;

import ca.wilkinsonlab.sadi.client.Registry;

/**
 * Unit tests for {@link SADIRegistries}.
 *
 * @author David Withers
 */
public class SADIRegistriesTest {

	private RegistryDetails registryDetails;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		registryDetails = new RegistryDetails("endpoint", "graphname");
		SADIRegistries.clear();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIRegistries#getRegistry(java.lang.String, java.lang.String)}.
	 * @throws IOException 
	 */
	@Test(expected=IOException.class)
	public void testGetRegistry() throws IOException {
		SADIRegistries.getRegistry(null, null);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIRegistries#getRegistries()}.
	 */
	@Test
	public void testGetRegistries() {
		Collection<Registry> registries = SADIRegistries.getRegistries();
		assertNotNull(registries);
		assertEquals(0, registries.size());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIRegistries#getRegistryMap()}.
	 */
	@Test
	public void testGetRegistryMap() {
		Map<RegistryDetails, Registry> registryMap = SADIRegistries.getRegistryMap();
		assertNotNull(registryMap);
		assertEquals(0, registryMap.size());
	}

	@Test
	public void testRegistryDetails() {
		assertNotNull(new RegistryDetails(null, null));
	}

	@Test
	public void testRegistryDetailsGetSparqlEndpoint() {
		assertEquals("endpoint", registryDetails.getSparqlEndpoint());
	}

	@Test
	public void testRegistryDetailsGetGraphName() {
		assertEquals("graphname", registryDetails.getGraphName());	
	}

	@Test
	public void testRegistryDetailsHashCode() {
		assertEquals(registryDetails.hashCode(), registryDetails.hashCode());
		assertEquals(registryDetails.hashCode(), new RegistryDetails("endpoint", "graphname").hashCode());	
	}

	@Test
	public void testRegistryDetailsEquals() {
		assertTrue(registryDetails.equals(registryDetails));
		assertTrue(registryDetails.equals(new RegistryDetails("endpoint", "graphname")));
		assertTrue(new RegistryDetails(null, null).equals(new RegistryDetails(null, null)));
		assertFalse(registryDetails.equals(new RegistryDetails("endpoint", "graph-name")));
		assertFalse(registryDetails.equals(new RegistryDetails("end-point", "graph-name")));
		assertFalse(registryDetails.equals(new RegistryDetails(null, "graph-name")));
		assertFalse(registryDetails.equals(new RegistryDetails(null, null)));
		assertFalse(registryDetails.equals(new RegistryDetails("end-point", null)));
		assertFalse(new RegistryDetails(null, "graphname").equals(new RegistryDetails(null, null)));
		assertFalse(new RegistryDetails("endpoint", null).equals(new RegistryDetails(null, null)));
		assertFalse(registryDetails.equals(""));
		assertFalse(registryDetails.equals(null));
	
	}

	@Test
	public void testClear() {
		SADIRegistries.clear();
		Collection<Registry> registries = SADIRegistries.getRegistries();
		assertNotNull(registries);
		assertEquals(0, registries.size());
		Map<RegistryDetails, Registry> registryMap = SADIRegistries.getRegistryMap();
		assertNotNull(registryMap);
		assertEquals(0, registryMap.size());
	}

}
