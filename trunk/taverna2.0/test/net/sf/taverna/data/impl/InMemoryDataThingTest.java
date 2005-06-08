/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.data.impl;

import junit.framework.TestCase;

import net.sf.taverna.data.*;

/**
 * Test the in memory DataThing implementation
 * @author Tom Oinn
 */
public class InMemoryDataThingTest extends TestCase {

    public void testConstruction() {
	DataThing d = new InMemoryDataThing("thing1", "Simple string");
	DataThing d2 = new InMemoryDataThing("thing2", new byte[100]);
	DataThing d3 = new InMemoryDataThing("thing3", new String[]{"hello","world","this","is","a","string","array"});
    }

    public void testLSID() throws Exception {
	DataThing d = new InMemoryDataThing("exampleLSID", new String[]{"hello","world","this","is","a","string","array"});
	assertTrue("LSID correctly assigned to root", d.getLSID().equals("exampleLSID"));
	DataThing related = d.getRelated(new int[]{3});
	assertTrue("Found value at index 3", related.getValue().equals("is"));
	assertTrue("LSID correctly assigned to child", related.getLSID().equals("exampleLSID.3"));
    }

    public void testRelation() throws Exception {
	DataThing d = new InMemoryDataThing("exampleLSID", new String[]{"hello","world","this","is","a","string","array"});
	DataThing child = d.getRelated(new int[]{3});
	DataThing childParent = child.getRelated(new int[0]);
	assertTrue("Parent -> Child -> Parent okay",childParent == d);
    }
    
}
