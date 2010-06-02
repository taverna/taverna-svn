/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: MartURLLocationTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007/01/31 14:12:14 $
 *               by   $Author: davidwithers $
 * Created on 02-Jun-2006
 *****************************************************************/
package org.biomart.martservice;

import junit.framework.TestCase;

/**
 *
 * @author David Withers
 */
public class MartURLLocationTest extends TestCase {
	private MartURLLocation martURLLocation; 
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		martURLLocation = new MartURLLocation();
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.isDefault()'
	 */
	public void testIsDefault() {
		assertFalse(martURLLocation.isDefault());
		martURLLocation.setDefault(true);
		assertTrue(martURLLocation.isDefault());
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.setDefault(boolean)'
	 */
	public void testSetDefault() {
		martURLLocation.setDefault(true);
		assertTrue(martURLLocation.isDefault());
		martURLLocation.setDefault(false);
		assertFalse(martURLLocation.isDefault());
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.getDisplayName()'
	 */
	public void testGetDisplayName() {
		assertNull(martURLLocation.getDisplayName());
		martURLLocation.setDisplayName("DisplayName");
		assertEquals(martURLLocation.getDisplayName(), "DisplayName");
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.setDisplayName(String)'
	 */
	public void testSetDisplayName() {
		martURLLocation.setDisplayName("DisplayName");
		assertEquals(martURLLocation.getDisplayName(), "DisplayName");
		martURLLocation.setDisplayName(null);
		assertNull(martURLLocation.getDisplayName());
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.getHost()'
	 */
	public void testGetHost() {
		assertNull(martURLLocation.getHost());
		martURLLocation.setHost("Host");
		assertEquals(martURLLocation.getHost(), "Host");
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.setHost(String)'
	 */
	public void testSetHost() {
		martURLLocation.setHost("Host");
		assertEquals(martURLLocation.getHost(), "Host");
		martURLLocation.setHost(null);
		assertNull(martURLLocation.getHost());
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.getName()'
	 */
	public void testGetName() {
		assertNull(martURLLocation.getName());
		martURLLocation.setName("Name");
		assertEquals(martURLLocation.getName(), "Name");
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.setName(String)'
	 */
	public void testSetName() {
		martURLLocation.setName("Name");
		assertEquals(martURLLocation.getName(), "Name");
		martURLLocation.setName(null);
		assertNull(martURLLocation.getName());
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.getPort()'
	 */
	public void testGetPort() {
		assertEquals(martURLLocation.getPort(), 0);
		martURLLocation.setPort(-1);
		assertEquals(martURLLocation.getPort(), -1);
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.setPort(int)'
	 */
	public void testSetPort() {
		martURLLocation.setPort(-1);
		assertEquals(martURLLocation.getPort(), -1);
		martURLLocation.setPort(0);
		assertEquals(martURLLocation.getPort(), 0);
		martURLLocation.setPort(1);
		assertEquals(martURLLocation.getPort(), 1);
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.getServerVirtualSchema()'
	 */
	public void testGetServerVirtualSchema() {
		assertNull(martURLLocation.getServerVirtualSchema());
		martURLLocation.setServerVirtualSchema("ServerVirtualSchema");
		assertEquals(martURLLocation.getServerVirtualSchema(), "ServerVirtualSchema");
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.setServerVirtualSchema(String)'
	 */
	public void testSetServerVirtualSchema() {
		martURLLocation.setServerVirtualSchema("ServerVirtualSchema");
		assertEquals(martURLLocation.getServerVirtualSchema(), "ServerVirtualSchema");
		martURLLocation.setServerVirtualSchema(null);
		assertNull(martURLLocation.getServerVirtualSchema());
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.getVirtualSchema()'
	 */
	public void testGetVirtualSchema() {
		assertNull(martURLLocation.getVirtualSchema());
		martURLLocation.setVirtualSchema("VirtualSchema");
		assertEquals(martURLLocation.getVirtualSchema(), "VirtualSchema");

	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.setVirtualSchema(String)'
	 */
	public void testSetVirtualSchema() {
		martURLLocation.setVirtualSchema("VirtualSchema");
		assertEquals(martURLLocation.getVirtualSchema(), "VirtualSchema");
		martURLLocation.setVirtualSchema(null);
		assertNull(martURLLocation.getVirtualSchema());
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.isVisible()'
	 */
	public void testIsVisible() {
		assertFalse(martURLLocation.isVisible());
		martURLLocation.setVisible(true);
		assertTrue(martURLLocation.isVisible());
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.setVisible(boolean)'
	 */
	public void testSetVisible() {
		martURLLocation.setVisible(true);
		assertTrue(martURLLocation.isVisible());
		martURLLocation.setVisible(false);
		assertFalse(martURLLocation.isVisible());
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.getType()'
	 */
	public void testGetType() {
		assertEquals(martURLLocation.getType(), "URL");
	}

	/*
	 * Test method for 'org.biomart.martservice.MartURLLocation.toString()'
	 */
	public void testToString() {
		martURLLocation.setDisplayName("DisplayName");
		assertEquals(martURLLocation.toString(), "DisplayName");
	}

	public void testHashCode() {
		MartURLLocation martURLLocation2 = new MartURLLocation();
		assertEquals(martURLLocation.hashCode(), martURLLocation2.hashCode());
		martURLLocation.setDefault(true);
		martURLLocation2.setDefault(true);
		assertEquals(martURLLocation.hashCode(), martURLLocation2.hashCode());
		martURLLocation.setDisplayName("DisplayName");
		martURLLocation2.setDisplayName("DisplayName");
		assertEquals(martURLLocation.hashCode(), martURLLocation2.hashCode());
		martURLLocation.setHost("Host");
		martURLLocation2.setHost("Host");
		assertEquals(martURLLocation.hashCode(), martURLLocation2.hashCode());
		martURLLocation.setName("Name");
		martURLLocation2.setName("Name");
		assertEquals(martURLLocation.hashCode(), martURLLocation2.hashCode());
		martURLLocation.setPort(-1);
		martURLLocation2.setPort(-1);
		assertEquals(martURLLocation.hashCode(), martURLLocation2.hashCode());
		martURLLocation.setServerVirtualSchema("ServerVirtualSchema");
		martURLLocation2.setServerVirtualSchema("ServerVirtualSchema");
		assertEquals(martURLLocation.hashCode(), martURLLocation2.hashCode());
		martURLLocation.setVirtualSchema("VirtualSchema");
		martURLLocation2.setVirtualSchema("VirtualSchema");
		assertEquals(martURLLocation.hashCode(), martURLLocation2.hashCode());
		martURLLocation.setVisible(true);
		martURLLocation2.setVisible(true);
		assertEquals(martURLLocation.hashCode(), martURLLocation2.hashCode());
		martURLLocation.setVisible(true);
		martURLLocation2.setVisible(true);
		assertEquals(martURLLocation.hashCode(), martURLLocation2.hashCode());
		
	}

}
