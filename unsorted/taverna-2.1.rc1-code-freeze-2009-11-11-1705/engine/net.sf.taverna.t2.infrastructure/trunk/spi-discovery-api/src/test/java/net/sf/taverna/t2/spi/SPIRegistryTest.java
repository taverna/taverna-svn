/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.spi;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class SPIRegistryTest {

	SPIRegistry<DummySPI> registry = new SPIRegistry<DummySPI>(DummySPI.class);
	
	@Test
	public void getInstances() {
		List<DummySPI> instances = registry.getInstances();
		// Test that they were instantiated
		
		Set<String> expected = new HashSet<String>();
		expected.add(FirstDummySPI.class.getSimpleName());
		expected.add(SecondDummySPI.class.getSimpleName());
		
		Set<String> found = new HashSet<String>();
		
		for (DummySPI spi : instances) {
			if (! found.add(spi.getClass().getSimpleName())) {
				fail("Duplicate SPI " + spi.getClass().getCanonicalName());
			}
			assertEquals("Wrong name", spi.getName(), spi.getClass().getSimpleName());
		}
		assertEquals("Wrong SPIs", expected, found);
		
	}

}
