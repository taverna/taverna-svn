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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.taverna.t2.activities.sadi.SADIActivity;
import net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean;

import org.junit.Before;
import org.junit.Test;

import ca.wilkinsonlab.sadi.beans.ServiceBean;

/**
 * Unit tests for {@link SADIServiceDescription}.
 *
 * @author David Withers
 */
public class SADIServiceDescriptionTest {

	private SADIServiceDescription sadiServiceDescription;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sadiServiceDescription = new SADIServiceDescription();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription#isTemplateService()}.
	 */
	@Test
	public void testIsTemplateService() {
		assertFalse(sadiServiceDescription.isTemplateService());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription#getSparqlEndpoint()}.
	 */
	@Test
	public void testGetSparqlEndpoint() {
		assertNull(sadiServiceDescription.getSparqlEndpoint());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription#setSparqlEndpoint(java.lang.String)}.
	 */
	@Test
	public void testSetSparqlEndpoint() {
		sadiServiceDescription.setSparqlEndpoint("endpoint");
		assertEquals("endpoint", sadiServiceDescription.getSparqlEndpoint());
		sadiServiceDescription.setSparqlEndpoint(null);
		assertNull(sadiServiceDescription.getSparqlEndpoint());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription#getGraphName()}.
	 */
	@Test
	public void testGetGraphName() {
		assertNull(sadiServiceDescription.getGraphName());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription#setGraphName(java.lang.String)}.
	 */
	@Test
	public void testSetGraphName() {
		sadiServiceDescription.setGraphName("graph");
		assertEquals("graph", sadiServiceDescription.getGraphName());
		sadiServiceDescription.setGraphName(null);
		assertNull(sadiServiceDescription.getGraphName());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription#getServiceInfo()}.
	 */
	@Test
	public void testGetServiceInfo() {
		assertNull(sadiServiceDescription.getServiceInfo());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription#setServiceURI(java.lang.String)}.
	 */
	@Test
	public void testSetServiceInfo() {
		ca.wilkinsonlab.sadi.ServiceDescription serviceBean = new ServiceBean();
		sadiServiceDescription.setServiceInfo(serviceBean);
		assertEquals(serviceBean, sadiServiceDescription.getServiceInfo());
		sadiServiceDescription.setServiceInfo(null);
		assertNull(sadiServiceDescription.getServiceInfo());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription#getActivityClass()}.
	 */
	@Test
	public void testGetActivityClass() {
		assertEquals(SADIActivity.class, sadiServiceDescription.getActivityClass());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription#getActivityConfiguration()}.
	 */
	@Test
	public void testGetActivityConfiguration() {
		assertEquals(new SADIActivityConfigurationBean(), sadiServiceDescription.getActivityConfiguration());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription#getIcon()}.
	 */
	@Test
	public void testGetIcon() {
		assertEquals(SADIActivityIcon.getSADIIcon(), sadiServiceDescription.getIcon());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription#getName()}.
	 */
	@Test
	public void testGetName() {
		assertNull(sadiServiceDescription.getName());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription#getPath()}.
	 */
	@Test
	public void testGetPath() {
		assertEquals(Collections.singletonList("SADI @ null"), sadiServiceDescription.getPath());
		sadiServiceDescription.setSparqlEndpoint("sparql endpoint");
		assertEquals(Collections.singletonList("SADI @ sparql endpoint"), sadiServiceDescription.getPath());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription#getIdentifyingData()}.
	 */
	@Test
	public void testGetIdentifyingData() {
		List<Object> identifyingData = Arrays.<Object>asList(null, null, null);
		assertEquals(identifyingData, sadiServiceDescription.getIdentifyingData());
		
		ServiceBean serviceBean = new ServiceBean();
		serviceBean.setURI("service-uri");
		sadiServiceDescription.setSparqlEndpoint("endpoint");
		sadiServiceDescription.setGraphName("graph");
		sadiServiceDescription.setServiceInfo(serviceBean);
		identifyingData = Arrays.<Object>asList("endpoint", "graph", "service-uri");
		assertEquals(identifyingData, sadiServiceDescription.getIdentifyingData());
	}

}
