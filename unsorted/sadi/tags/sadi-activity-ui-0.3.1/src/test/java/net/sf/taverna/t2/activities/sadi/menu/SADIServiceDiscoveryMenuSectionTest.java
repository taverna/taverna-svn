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
package net.sf.taverna.t2.activities.sadi.menu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.sadi.SADIActivityInputPort;
import net.sf.taverna.t2.ui.menu.ContextualSelection;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link SADIServiceDiscoveryMenuSection}.
 *
 * @author David Withers
 */
public class SADIServiceDiscoveryMenuSectionTest {

	private SADIServiceDiscoveryMenuSection serviceDiscoveryMenuSection;
	
	private ContextualSelection contextualSelection;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		serviceDiscoveryMenuSection = new SADIServiceDiscoveryMenuSection();
		contextualSelection = new ContextualSelection(null, new SADIActivityInputPort(null, null, null, 0), null);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.menu.SADIServiceDiscoveryMenuSection#SADIServiceDiscoveryMenuSection()}.
	 */
	@Test
	public void testSADIServiceDiscoveryMenuSection() {
		new SADIServiceDiscoveryMenuSection();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.menu.SADIServiceDiscoveryMenuSection#isEnabled()}.
	 */
	@Test
	public void testIsEnabled() {
		assertFalse(serviceDiscoveryMenuSection.isEnabled());
		serviceDiscoveryMenuSection.setContextualSelection(contextualSelection);
		assertTrue(serviceDiscoveryMenuSection.isEnabled());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.menu.SADIServiceDiscoveryMenuSection#createAction()}.
	 */
	@Test
	public void testCreateAction() {
		assertNotNull(serviceDiscoveryMenuSection.createAction());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.menu.SADIServiceDiscoveryMenuSection#getContextualSelection()}.
	 */
	@Test
	public void testGetContextualSelection() {
		assertNull(serviceDiscoveryMenuSection.getContextualSelection());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.menu.SADIServiceDiscoveryMenuSection#setContextualSelection(net.sf.taverna.t2.ui.menu.ContextualSelection)}.
	 */
	@Test
	public void testSetContextualSelection() {
		serviceDiscoveryMenuSection.setContextualSelection(contextualSelection);
		assertEquals(contextualSelection, serviceDiscoveryMenuSection.getContextualSelection());
		serviceDiscoveryMenuSection.setContextualSelection(null);
		assertNull(serviceDiscoveryMenuSection.getContextualSelection());
	}

}
