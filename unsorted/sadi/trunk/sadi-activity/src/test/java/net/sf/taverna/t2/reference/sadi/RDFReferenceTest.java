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

import net.sf.taverna.t2.reference.ReferencedDataNature;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

/**
 * 
 * 
 * @author David Withers
 */
public class RDFReferenceTest {

	private RDFReference rdfReference;

	private RDFNode rdfNode, rdfURIResourceNode, rdfResourceStringNode;

	private String rdfResourceString, rdfURIResourceString;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		rdfReference = new RDFReference();
		rdfNode = ModelFactory.createDefaultModel().createResource("http://example.com/example");
		rdfURIResourceString =
			"<http://purl.uniprot.org/uniprot/P12345>\n" +
			"@prefix lsrn:    <http://purl.oclc.org/SADI/LSRN/> .\n" + 
			"<http://purl.uniprot.org/uniprot/P12345>\n" + 
			"      a       lsrn:UniProt_Record .";
		rdfURIResourceNode = ResourceFactory.createResource("http://purl.uniprot.org/uniprot/P12345");
		rdfResourceString = 
			"<http://sadiframework.org/examples/input/erminej1>\n" + 
			"@prefix ermineJ:  <http://sadiframework.org/examples/ermineJ.owl#> .\n" + 
			"\n" + 
			"<http://sadiframework.org/examples/input/erminej1>\n" + 
			"      a       ermineJ:AnnotatedProbeSet ;\n" + 
			"      ermineJ:hasOverrepresentedTerm\n" + 
			"              [ a       ermineJ:OverrepresentedTerm ;\n" + 
			"                ermineJ:p \"0.33333333333333326\"^^<http://www.w3.org/2001/XMLSchema#double> ;\n" + 
			"                ermineJ:term <http://biordf.net/moby/GO/0005515>\n" + 
			"              ] .";
		rdfResourceStringNode = ResourceFactory.createResource("http://sadiframework.org/examples/input/erminej1"); 
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
		assertEquals("<http://example.com/example>", new BufferedReader(new InputStreamReader(stream)).readLine());

		rdfReference.setContents(rdfURIResourceString);
		stream = rdfReference.openStream(null);
		assertEquals("<http://purl.uniprot.org/uniprot/P12345>", new BufferedReader(new InputStreamReader(stream)).readLine());

		rdfReference.setContents(rdfResourceString);
		stream = rdfReference.openStream(null);
		assertEquals("<http://sadiframework.org/examples/input/erminej1>", new BufferedReader(new InputStreamReader(stream)).readLine());
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
		assertEquals(ResourceFactory.createPlainLiteral("5"), rdfReference.getValue());
		rdfReference.setContents("5^^http://www.w3.org/2001/XMLSchema#int");
		assertEquals(ResourceFactory.createTypedLiteral(5), rdfReference.getValue());
		rdfReference.setContents(rdfURIResourceString);
		assertEquals(rdfURIResourceNode, rdfReference.getValue());
		rdfReference.setContents(rdfResourceString);
		assertEquals(rdfResourceStringNode, rdfReference.getValue());
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
		rdfReference.setValue(ModelFactory.createDefaultModel().createTypedLiteral(5));
		assertEquals("5^^http://www.w3.org/2001/XMLSchema#int", rdfReference.getContents());
		rdfReference.setValue(ModelFactory.createDefaultModel().createResource());
		assertTrue("anonymous resource was not nonymized", rdfReference.getContents().startsWith("<"));
	}
	
	@Test
	public void testBackAndForth() {
		Model model = ModelFactory.createDefaultModel();
		Resource guy = model.createResource("http://example.com/foo", FOAF.Person);
		guy.addProperty(FOAF.name, "Guy Incognito");
		Resource joey = model.createResource("http://example.com/bar", FOAF.Person);
		joey.addProperty(FOAF.name, "Joey Jojo Jr. Shabadoo");
		guy.addProperty(FOAF.knows, joey);
		
		rdfReference.setValue(guy);
		String contentsAsString = rdfReference.getContents();
		rdfReference = new RDFReference();
		rdfReference.setContents(contentsAsString);
		Resource value = rdfReference.getValue().asResource();
		assertEquals("resource are not equal after conversion", guy, value);
		assertTrue("models are not isomorphic after conversion", model.isIsomorphicWith(value.getModel()));
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
