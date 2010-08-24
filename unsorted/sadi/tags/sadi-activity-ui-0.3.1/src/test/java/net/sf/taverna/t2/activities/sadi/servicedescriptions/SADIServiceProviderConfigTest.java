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
package net.sf.taverna.t2.activities.sadi.servicedescriptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link SADIServiceProviderConfig}.
 *
 * @author David Withers
 */
public class SADIServiceProviderConfigTest {

	private SADIServiceProviderConfig config, config2;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		config = new SADIServiceProviderConfig();
		config2 = new SADIServiceProviderConfig("endpoint", "graph");
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProviderConfig#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		assertEquals(config.hashCode(), config.hashCode());
		assertEquals(config.hashCode(), new SADIServiceProviderConfig().hashCode());
		assertEquals(config2.hashCode(), config2.hashCode());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProviderConfig#SADIServiceProviderConfig()}.
	 */
	@Test
	public void testSADIServiceProviderConfig() {
		new SADIServiceProviderConfig();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProviderConfig#SADIServiceProviderConfig(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSADIServiceProviderConfigStringString() {
		assertEquals(config, new SADIServiceProviderConfig(null, null));
		assertEquals(config2, new SADIServiceProviderConfig("endpoint", "graph"));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProviderConfig#getSparqlEndpoint()}.
	 */
	@Test
	public void testGetSparqlEndpoint() {
		assertNull(config.getSparqlEndpoint());
		assertEquals("endpoint", config2.getSparqlEndpoint());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProviderConfig#setSparqlEndpoint(java.lang.String)}.
	 */
	@Test
	public void testSetSparqlEndpoint() {
		config.setSparqlEndpoint("sparqlEndpoint");
		assertEquals("sparqlEndpoint", config.getSparqlEndpoint());
		config.setSparqlEndpoint(null);
		assertNull(config.getSparqlEndpoint());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProviderConfig#getGraphName()}.
	 */
	@Test
	public void testGetGraphName() {
		assertNull(config.getGraphName());
		assertEquals("graph", config2.getGraphName());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProviderConfig#setGraphName(java.lang.String)}.
	 */
	@Test
	public void testSetGraphName() {
		config.setGraphName("graphName");
		assertEquals("graphName", config.getGraphName());
		config.setGraphName(null);
		assertNull(config.getGraphName());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProviderConfig#toString()}.
	 */
	@Test
	public void testToString() {
		assertNull(config.toString());
		assertEquals("endpoint", config2.toString());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProviderConfig#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		assertTrue(config.equals(config));
		assertTrue(config2.equals(config2));
		assertFalse(config.equals(config2));
		assertFalse(config2.equals(config));
		assertFalse(config.equals(""));
		assertFalse(config.equals(null));
	}

}
