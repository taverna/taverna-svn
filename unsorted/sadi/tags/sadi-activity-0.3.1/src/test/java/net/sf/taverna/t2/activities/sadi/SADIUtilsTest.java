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

import org.junit.Test;

import com.hp.hpl.jena.graph.Node;

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
	public void test() {
//		Literal literal = Model.createPlainLiteral("0.33333333333333326^^http://www.w3.org/2001/XMLSchema#double");
		Node literal = Node.createLiteral("0.33333333333333326^^http://www.w3.org/2001/XMLSchema#double", null, false);
		System.out.println(literal.getLiteral());
//		System.out.println(literal.getLexicalForm());
//		System.out.println(literal.getDatatypeURI());
	}
	
}
