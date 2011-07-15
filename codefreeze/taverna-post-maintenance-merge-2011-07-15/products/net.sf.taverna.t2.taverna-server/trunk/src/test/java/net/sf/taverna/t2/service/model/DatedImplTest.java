/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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
package net.sf.taverna.t2.service.model;

import static org.junit.Assert.*;

import java.util.Date;

import net.sf.taverna.t2.service.model.DatedImpl;

import org.junit.Before;
import org.junit.Test;

/**
 *
 *
 * @author David Withers
 */
public class DatedImplTest {

	private DatedImpl dated;

	@Before
	public void setUp() throws Exception {
		dated = new DatedImpl();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.model.DatedImpl#getModified()}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testGetLastModified() throws InterruptedException {
		assertNotNull(dated.getModified());
		assertEquals(dated.getCreated(), dated.getModified());
		Thread.sleep(500);
		dated.updateModified();
		assertTrue(dated.getModified().after(dated.getCreated()));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.model.DatedImpl#updateModified()}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testSetLastModified() throws InterruptedException {
		Date modified = dated.getModified();
		assertEquals(modified, dated.getCreated());
		Thread.sleep(500);
		dated.updateModified();
		assertTrue(dated.getModified().after(modified));
		modified = dated.getModified();
		Thread.sleep(500);
		dated.updateModified();
		assertTrue(dated.getModified().after(modified));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.model.DatedImpl#getCreated()}.
	 */
	@Test
	public void testGetCreated() {
		assertNotNull(dated.getCreated());
	}

}
