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

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link SADIServiceProvider}.
 *
 * @author David Withers
 */
public class SADIServiceProviderTest {

	private SADIServiceProvider sadiServiceProvider;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sadiServiceProvider = new SADIServiceProvider();
		sadiServiceProvider.configure(sadiServiceProvider.getDefaultConfigurations().get(0));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProvider#SADIServiceProvider()}.
	 */
	@Test
	public void testSADIServiceProvider() {
		new SADIServiceProvider();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProvider#getIcon()}.
	 */
	@Test
	public void testGetIcon() {
		assertEquals(SADIActivityIcon.getSADIIcon(), sadiServiceProvider.getIcon());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProvider#getName()}.
	 */
	@Test
	public void testGetName() {
		assertEquals("SADI", sadiServiceProvider.getName());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProvider#getDefaultConfigurations()}.
	 */
	@Test
	public void testGetDefaultConfigurations() {
		assertEquals(1, sadiServiceProvider.getDefaultConfigurations().size());
		assertEquals(new SADIServiceProviderConfig("http://biordf.net/sparql", "http://sadiframework.org/registry/"),
				sadiServiceProvider.getDefaultConfigurations().get(0));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProvider#getIdentifyingData()}.
	 */
	@Test
	public void testGetIdentifyingData() {
		assertEquals(1, sadiServiceProvider.getIdentifyingData().size());
		assertEquals("http://biordf.net/sparql", sadiServiceProvider.getIdentifyingData().get(0));
	}

}
