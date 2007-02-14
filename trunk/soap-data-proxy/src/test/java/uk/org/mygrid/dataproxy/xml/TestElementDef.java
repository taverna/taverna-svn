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
 * Filename           $RCSfile: TestElementDef.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-14 15:30:03 $
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

public class TestElementDef {

	@Test
	public void testEquals() {
		ElementDef a = new ElementDef("a","namespace");
		ElementDef a2 = new ElementDef("a","namespace");
		
		ElementDef b = new ElementDef("b","namespace");
		ElementDef b_nons = new ElementDef("b","");
		
		assertTrue(a.equals(a2));
		assertTrue(a2.equals(a));
		
		assertFalse(a.equals(b));
		assertFalse(b.equals(a));
		assertFalse(b.equals(b_nons));
	}
	
	@Test
	public void testMap() {
		ElementDef a = new ElementDef("a","namespace");
		ElementDef a2 = new ElementDef("a","namespace");
		
		ElementDef b = new ElementDef("b","namespace");
		ElementDef b_nons = new ElementDef("b","");
		
		Map<ElementDef,String> map= new HashMap<ElementDef,String>();
		
		map.put(a, "a");		
		map.put(b, "b");
		map.put(b_nons, "b_nons");
		
		assertEquals("a",map.get(a));
		assertEquals("a",map.get(a2));
		assertEquals("a",map.get(new ElementDef("a","namespace")));
		
				
		assertEquals("b",map.get(b));
		assertEquals("b_nons",map.get(b_nons));
		assertEquals("b_nons",map.get(new ElementDef("b","")));
		
	}
	
	
	
}
