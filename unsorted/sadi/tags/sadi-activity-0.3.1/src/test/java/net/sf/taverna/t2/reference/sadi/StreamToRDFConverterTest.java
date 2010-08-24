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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * 
 * 
 * @author David Withers
 */
public class StreamToRDFConverterTest {

	private StreamToRDFConverter streamToRDFConverter;

	private String rdfResourceString, rdfURIResourceString;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		streamToRDFConverter = new StreamToRDFConverter();
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
	 * {@link net.sf.taverna.t2.reference.sadi.StreamToRDFConverter#convert(java.io.InputStream)}
	 * .
	 */
	@Test
	public void testConvert() {
		RDFNode rdfNode = StreamToRDFConverter.convert(new ByteArrayInputStream(rdfResourceString.getBytes()));
		assertTrue(rdfNode.isURIResource());
		assertEquals("http://sadiframework.org/examples/input/erminej1", rdfNode.asNode().getURI());
		rdfNode = StreamToRDFConverter.convert(new ByteArrayInputStream(rdfURIResourceString.getBytes()));
		assertTrue(rdfNode.isURIResource());
		assertEquals("http://purl.uniprot.org/uniprot/P12345", rdfNode.asNode().getURI());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.reference.sadi.StreamToRDFConverter#getPojoClass()}
	 * .
	 */
	@Test
	public void testGetPojoClass() {
		assertEquals(RDFNode.class, streamToRDFConverter.getPojoClass());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.reference.sadi.StreamToRDFConverter#renderFrom(java.io.InputStream)}
	 * .
	 */
	@Test
	public void testRenderFrom() {
		RDFNode rdfNode = streamToRDFConverter.renderFrom(new ByteArrayInputStream(rdfResourceString.getBytes()));
		assertTrue(rdfNode.isURIResource());
		assertEquals("http://sadiframework.org/examples/input/erminej1", rdfNode.asNode().getURI());
		rdfNode = streamToRDFConverter.renderFrom(new ByteArrayInputStream(rdfURIResourceString.getBytes()));
		assertTrue(rdfNode.isURIResource());
		assertEquals("http://purl.uniprot.org/uniprot/P12345", rdfNode.asNode().getURI());
	}

}
