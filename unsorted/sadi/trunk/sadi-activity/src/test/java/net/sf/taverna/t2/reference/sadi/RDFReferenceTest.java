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
package net.sf.taverna.t2.reference.sadi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import net.sf.taverna.t2.reference.ReferencedDataNature;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * 
 * 
 * @author David Withers
 */
public class RDFReferenceTest {

	private RDFReference rdfReference;

	private RDFNode rdfNode;

	private String rdfResourceString, rdfURIResourceString;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		rdfReference = new RDFReference();
		rdfNode = ModelFactory.createDefaultModel().createResource("http://example.com/example");
		rdfURIResourceString = "<rdf:RDF\n"
				+ "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "xmlns:lsrn=\"http://purl.oclc.org/SADI/LSRN/\"\n"
				+ "xmlns:uniprotInfo=\"http://sadiframework.org/examples/uniprotInfo.owl#\">\n"
				+ "<lsrn:UniProt_Record rdf:about=\"http://purl.uniprot.org/uniprot/P12345\">\n"
				+ "</lsrn:UniProt_Record>\n" + "</rdf:RDF>";

		rdfResourceString = "<rdf:RDF\n"
				+ "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
				+ "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "xmlns:ermineJ=\"http://sadiframework.org/examples/ermineJ.owl#\">\n"
				+ "<ermineJ:AnnotatedProbeSet rdf:about=\"http://sadiframework.org/examples/input/erminej1\">\n"
				+ "<ermineJ:hasOverrepresentedTerm>\n"
				+ "<ermineJ:OverrepresentedTerm>\n"
				+ "<ermineJ:term rdf:resource=\"http://biordf.net/moby/GO/0005515\"/>\n"
				+ "<ermineJ:p rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">0.33333333333333326</ermineJ:p>\n"
				+ "</ermineJ:OverrepresentedTerm>\n" + "</ermineJ:hasOverrepresentedTerm>\n"
				+ "</ermineJ:AnnotatedProbeSet>\n" + "</rdf:RDF>";
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.reference.sadi.RDFReference#getResolutionCost()}
	 * .
	 */
	@Test
	public void testGetResolutionCost() {
		assertEquals(0, rdfReference.getResolutionCost(), 0);
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.reference.sadi.RDFReference#openStream(net.sf.taverna.t2.reference.ReferenceContext)}
	 * .
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOpenStream() throws Exception {
		InputStream stream = rdfReference.openStream(null);
		assertEquals(-1, stream.read());

		rdfReference.setContents("literal");
		stream = rdfReference.openStream(null);
		assertEquals("literal", new BufferedReader(new InputStreamReader(stream)).readLine());

		rdfReference.setValue(rdfNode);
		stream = rdfReference.openStream(null);
		assertEquals("http://example.com/example", new BufferedReader(new InputStreamReader(stream)).readLine());
//		assertEquals("<rdf:RDF", new BufferedReader(new InputStreamReader(stream)).readLine());

		rdfReference.setContents(rdfURIResourceString);
		stream = rdfReference.openStream(null);
		assertEquals("http://purl.uniprot.org/uniprot/P12345", new BufferedReader(new InputStreamReader(stream)).readLine());
//		assertEquals("<rdf:RDF", new BufferedReader(new InputStreamReader(stream)).readLine());

		rdfReference.setContents(rdfResourceString);
		stream = rdfReference.openStream(null);
		assertEquals("http://sadiframework.org/examples/input/erminej1", new BufferedReader(new InputStreamReader(stream)).readLine());
//		assertEquals("<rdf:RDF", new BufferedReader(new InputStreamReader(stream)).readLine());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.reference.sadi.RDFReference#getDataNature()}.
	 */
	@Test
	public void testGetDataNature() {
		assertEquals(ReferencedDataNature.TEXT, rdfReference.getDataNature());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.reference.sadi.RDFReference#getCharset()}.
	 */
	@Test
	public void testGetCharset() {
		assertEquals("UTF-8", rdfReference.getCharset());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.reference.sadi.RDFReference#toString()}.
	 */
	@Test
	public void testToString() {
		assertEquals("node{null}", rdfReference.toString());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.reference.sadi.RDFReference#getContents()}.
	 */
	@Test
	public void testGetContents() {
		assertEquals("", rdfReference.getContents());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.reference.sadi.RDFReference#setContents(java.lang.String)}
	 * .
	 */
	@Test
	public void testSetContents() {
		rdfReference.setContents("5");
		assertEquals("5", rdfReference.getContents());
		rdfReference.setContents(rdfURIResourceString);
		assertTrue(rdfReference.getContents().contains(
				"UniProt_Record rdf:about=\"http://purl.uniprot.org/uniprot/P12345\""));

		rdfReference.setContents(rdfResourceString);
		Model model1 = ModelFactory.createDefaultModel().read(new StringReader(rdfResourceString),
				null);
		Model model2 = ModelFactory.createDefaultModel().read(
				new StringReader(rdfReference.getContents()), null);
		assertTrue(model1.isIsomorphicWith(model2));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.reference.sadi.RDFReference#getValue()}.
	 */
	@Test
	public void testGetValue() {
		assertNull(rdfReference.getValue());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.reference.sadi.RDFReference#setValue(com.hp.hpl.jena.rdf.model.RDFNode)}
	 * .
	 */
	@Test
	public void testSetValue() {
		rdfReference.setValue(rdfNode);
		assertEquals(rdfNode, rdfReference.getValue());
		rdfReference.setValue(null);
		assertNull(rdfReference.getValue());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.reference.sadi.RDFReference#getValueType()}.
	 */
	@Test
	public void testGetValueType() {
		assertEquals(RDFNode.class, rdfReference.getValueType());
	}

}
