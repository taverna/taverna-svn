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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link SADIActivityConfigurationBean}.
 *
 * @author David Withers
 */
public class SADIActivityConfigurationBeanTest {

	private SADIActivityConfigurationBean configurationBean, configurationBean2;
	
	private List<List<String>> restrictionPaths;
	
	private List<String> restrictionPath;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		configurationBean = new SADIActivityConfigurationBean();
		configurationBean2 = new SADIActivityConfigurationBean();
		configurationBean2.setGraphName("graph-name");
		configurationBean2.setSparqlEndpoint("sparql-endpoint");
		configurationBean2.setServiceURI("service-url");
		configurationBean2.setAttribute("a", "b");
		restrictionPath = new ArrayList<String>();
		restrictionPath.add("root");
		restrictionPath.add("leaf");
		configurationBean2.addInputRestrictionPath(restrictionPath);
		configurationBean2.addOutputRestrictionPath(restrictionPath);
		restrictionPaths = new ArrayList<List<String>>();
		restrictionPaths.add(restrictionPath);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		assertEquals(configurationBean.hashCode(), configurationBean.hashCode());
		assertEquals(configurationBean.hashCode(), new SADIActivityConfigurationBean(configurationBean).hashCode());
		assertEquals(configurationBean2.hashCode(), configurationBean2.hashCode());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#SADIActivityConfigurationBean()}.
	 */
	@Test
	public void testSADIActivityConfigurationBean() {
		new SADIActivityConfigurationBean();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#SADIActivityConfigurationBean(net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean)}.
	 */
	@Test
	public void testSADIActivityConfigurationBeanSADIActivityConfigurationBean() {
		assertEquals(configurationBean, new SADIActivityConfigurationBean(configurationBean));
		assertFalse(configurationBean.equals(new SADIActivityConfigurationBean(configurationBean2)));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#getSparqlEndpoint()}.
	 */
	@Test
	public void testGetSparqlEndpoint() {
		assertNull(configurationBean.getSparqlEndpoint());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#setSparqlEndpoint(java.lang.String)}.
	 */
	@Test
	public void testSetSparqlEndpoint() {
		configurationBean.setSparqlEndpoint("endpoint");
		assertEquals("endpoint", configurationBean.getSparqlEndpoint());
		configurationBean.setSparqlEndpoint(null);
		assertNull(configurationBean.getSparqlEndpoint());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#getGraphName()}.
	 */
	@Test
	public void testGetGraphName() {
		assertNull(configurationBean.getGraphName());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#setGraphName(java.lang.String)}.
	 */
	@Test
	public void testSetGraphName() {
		configurationBean.setGraphName("graph-name");
		assertEquals("graph-name", configurationBean.getGraphName());
		configurationBean.setGraphName(null);
		assertNull(configurationBean.getGraphName());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#getServiceURI()}.
	 */
	@Test
	public void testGetServiceURI() {
		assertNull(configurationBean.getServiceURI());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#setServiceURI(java.lang.String)}.
	 */
	@Test
	public void testSetServiceURI() {
		configurationBean.setServiceURI("graph-name");
		assertEquals("graph-name", configurationBean.getServiceURI());
		configurationBean.setServiceURI(null);
		assertNull(configurationBean.getServiceURI());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#getInputRestrictionPaths()}.
	 */
	@Test
	public void testGetInputRestrictionPaths() {
		assertNotNull(configurationBean.getInputRestrictionPaths());
		assertEquals(Collections.EMPTY_LIST, configurationBean.getInputRestrictionPaths());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#setInputRestrictionPaths(java.util.List)}.
	 */
	@Test
	public void testSetInputRestrictionPaths() {
		configurationBean.setInputRestrictionPaths(restrictionPaths);
		assertEquals(restrictionPaths, configurationBean.getInputRestrictionPaths());
		assertNotSame(restrictionPaths, configurationBean.getInputRestrictionPaths());
		restrictionPaths.clear();
		assertFalse(restrictionPaths.equals(configurationBean.getInputRestrictionPaths()));
		configurationBean.setInputRestrictionPaths(null);
		assertNotNull(configurationBean.getInputRestrictionPaths());
		assertEquals(Collections.EMPTY_LIST, configurationBean.getInputRestrictionPaths());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#getOutputRestrictionPaths()}.
	 */
	@Test
	public void testGetOutputRestrictionPaths() {
		assertNotNull(configurationBean.getOutputRestrictionPaths());
		assertEquals(Collections.EMPTY_LIST, configurationBean.getOutputRestrictionPaths());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#setOutputRestrictionPaths(java.util.List)}.
	 */
	@Test
	public void testSetOutputRestrictionPaths() {
		configurationBean.setOutputRestrictionPaths(restrictionPaths);
		assertEquals(restrictionPaths, configurationBean.getOutputRestrictionPaths());
		assertNotSame(restrictionPaths, configurationBean.getOutputRestrictionPaths());
		restrictionPaths.clear();
		assertFalse(restrictionPaths.equals(configurationBean.getOutputRestrictionPaths()));
		configurationBean.setOutputRestrictionPaths(null);
		assertNotNull(configurationBean.getOutputRestrictionPaths());
		assertEquals(Collections.EMPTY_LIST, configurationBean.getOutputRestrictionPaths());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#addInputRestrictionPath(java.util.List)}.
	 */
	@Test
	public void testAddInputRestrictionPath() {
		configurationBean.addInputRestrictionPath(restrictionPath);
		assertEquals(restrictionPaths, configurationBean.getInputRestrictionPaths());
		configurationBean.addInputRestrictionPath(restrictionPath);
		restrictionPaths.add(restrictionPath);
		assertEquals(restrictionPaths, configurationBean.getInputRestrictionPaths());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#removeInputRestrictionPath(java.util.List)}.
	 */
	@Test
	public void testRemoveInputRestrictionPath() {
		configurationBean.setInputRestrictionPaths(restrictionPaths);
		configurationBean.removeInputRestrictionPath(restrictionPath);
		assertEquals(Collections.EMPTY_LIST, configurationBean.getInputRestrictionPaths());
		configurationBean.removeInputRestrictionPath(restrictionPath);
		assertEquals(Collections.EMPTY_LIST, configurationBean.getInputRestrictionPaths());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#addOutputRestrictionPath(java.util.List)}.
	 */
	@Test
	public void testAddOutputRestrictionPath() {
		configurationBean.addOutputRestrictionPath(restrictionPath);
		assertEquals(restrictionPaths, configurationBean.getOutputRestrictionPaths());
		configurationBean.addOutputRestrictionPath(restrictionPath);
		restrictionPaths.add(restrictionPath);
		assertEquals(restrictionPaths, configurationBean.getOutputRestrictionPaths());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#removeOutputRestrictionPath(java.util.List)}.
	 */
	@Test
	public void testRemoveOutputRestrictionPath() {
		configurationBean.setInputRestrictionPaths(restrictionPaths);
		configurationBean.removeOutputRestrictionPath(restrictionPath);
		assertEquals(Collections.EMPTY_LIST, configurationBean.getOutputRestrictionPaths());
		configurationBean.removeOutputRestrictionPath(restrictionPath);
		assertEquals(Collections.EMPTY_LIST, configurationBean.getOutputRestrictionPaths());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		assertTrue(configurationBean.equals(configurationBean));
		assertTrue(configurationBean2.equals(configurationBean2));
		assertFalse(configurationBean.equals(configurationBean2));
		assertFalse(configurationBean2.equals(configurationBean));
		assertFalse(configurationBean.equals(""));
		assertFalse(configurationBean.equals(null));
	}

	@Test
	public void testGetAttribute() {
		assertNull(configurationBean.getAttribute("b"));
		assertEquals("b", configurationBean2.getAttribute("a"));
		assertNull(configurationBean2.getAttribute("b"));
	}

	@Test
	public void testSetAttribute() {
		configurationBean.setAttribute("name", "value");
		assertEquals("value", configurationBean.getAttribute("name"));
		configurationBean.setAttribute("name", null);
		assertNull(configurationBean.getAttribute("name"));
		configurationBean.setAttribute("name2", null);
		assertNull(configurationBean.getAttribute("name2"));
	}

	@Test
	public void testRemoveAttribute() {
		configurationBean2.removeAttribute("a");
		assertNull(configurationBean2.getAttribute("a"));
	}

	@Test
	public void testGetAttributes() {
		assertEquals(Collections.singletonMap("a", "b"), configurationBean2.getAttributes());
	}

}
