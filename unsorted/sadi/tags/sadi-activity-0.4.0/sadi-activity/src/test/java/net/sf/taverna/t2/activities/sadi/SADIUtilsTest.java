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
package net.sf.taverna.t2.activities.sadi;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ca.wilkinsonlab.sadi.rdfpath.RDFPath;

import com.hp.hpl.jena.sparql.vocabulary.FOAF;

/**
 * Unit tests for SADIUtils
 *
 * @author David Withers
 */
public class SADIUtilsTest {

	@Test
	public void testUriToId() {
		assertEquals("2CPQ", SADIUtils.uriToId("http://lsrn.org/PDB:2CPQ"));
		assertEquals("2CPQ", SADIUtils.uriToId("http://lsrn.org/PDB#2CPQ"));
		assertEquals("2CPQ", SADIUtils.uriToId("http://lsrn.org/PDB:xxx#2CPQ"));
		assertEquals("2CPQ", SADIUtils.uriToId("http://example.org/PDB#2CPQ"));
		assertEquals("2CPQ", SADIUtils.uriToId("http://example.org/PDB/2CPQ"));
	}

	@Test
	public void testReplacePortMap() throws Exception {
		RDFPath name = new RDFPath(FOAF.name, null);
		RDFPath knowsName = new RDFPath(FOAF.knows, FOAF.Person, FOAF.name, null);
		RDFPath fundedByName = new RDFPath(FOAF.fundedBy, FOAF.Person, FOAF.name, null);
		Collection<RDFPath> paths = new ArrayList<RDFPath>();
		paths.add(name);
		paths.add(knowsName);
		paths.add(fundedByName);
		Map<String, String> expected = new HashMap<String, String>();
		expected.put("name", name.toString());
		expected.put("knows some Person name", knowsName.toString());
		expected.put("fundedBy some Person name", fundedByName.toString());
		Map<String, String> uniquePortMap = SADIUtils.buildPortMap(paths, "port");
		assertEquals(expected, uniquePortMap);
	}
	
}
