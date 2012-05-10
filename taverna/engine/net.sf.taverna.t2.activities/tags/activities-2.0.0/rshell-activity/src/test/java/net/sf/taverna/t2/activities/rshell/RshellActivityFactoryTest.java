/*******************************************************************************
 * Copyright (C) 2011 The University of Manchester   
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
package net.sf.taverna.t2.activities.rshell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author David Withers
 */
public class RshellActivityFactoryTest {

	private RshellActivityFactory factory;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		factory = new RshellActivityFactory();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.rshell.RshellActivityFactory#createActivity()}.
	 */
	@Test
	public void testCreateActivity() {
		RshellActivity createActivity = factory.createActivity();
		assertNotNull(createActivity);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.rshell.RshellActivityFactory#getActivityURI()}.
	 */
	@Test
	public void testGetActivityURI() {
		assertEquals(URI.create(RshellActivity.URI), factory.getActivityURI());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.rshell.RshellActivityFactory#createActivityConfiguration()}.
	 */
	@Test
	public void testCreateActivityConfiguration() {
		assertTrue(factory.createActivityConfiguration() instanceof RshellActivityConfigurationBean);
	}

}
