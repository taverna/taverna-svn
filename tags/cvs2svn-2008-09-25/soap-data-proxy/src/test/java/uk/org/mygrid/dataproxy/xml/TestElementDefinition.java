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
 * Filename           $RCSfile: TestElementDefinition.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-16 13:53:15 $
 *               by   $Author: sowen70 $
 * Created on 14 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestElementDefinition {

	@Test
	public void testEquals() {
		ElementDefinition a = new ElementDefinition("a","namespace","*/a","operation");
		ElementDefinition a2 = new ElementDefinition("a","namespace","*/a","operation");
		
		ElementDefinition b = new ElementDefinition("b","namespace","*/b","operation");
		ElementDefinition b_nons = new ElementDefinition("b","","*/b","operation");
		
		assertTrue(a.equals(a2));
		assertTrue(a2.equals(a));
		
		assertFalse(a.equals(b));
		assertFalse(b.equals(a));
		assertFalse(b.equals(b_nons));
	}
	
	@Test
	public void testMap() {
		ElementDefinition a = new ElementDefinition("a","namespace","*/a","operation");
		ElementDefinition a2 = new ElementDefinition("a","namespace","*/a","operation");
		
		ElementDefinition b = new ElementDefinition("b","namespace","*/b","operation");
		ElementDefinition b_nons = new ElementDefinition("b","","*/b","operation");
		
		Map<ElementDefinition,String> map= new HashMap<ElementDefinition,String>();
		
		map.put(a, "a");		
		map.put(b, "b");
		map.put(b_nons, "b_nons");
		
		assertEquals("a",map.get(a));
		assertEquals("a",map.get(a2));
		assertEquals("a",map.get(new ElementDefinition("a","namespace","*/a","operation")));
		
				
		assertEquals("b",map.get(b));
		assertEquals("b_nons",map.get(b_nons));
		assertEquals("b_nons",map.get(new ElementDefinition("b","","*/b","operation")));		
	}	
	
	@Test
	public void testPathAndOpIgnored() throws Exception {
		ElementDefinition a = new ElementDefinition("a","namespace","*/a","operation");
		ElementDefinition b = new ElementDefinition("a","namespace","sdfsdfsdfsdf","kkkkk");
		
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
	}	
}
