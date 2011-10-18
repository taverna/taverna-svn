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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import ca.wilkinsonlab.sadi.rdfpath.RDFPath;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.impl.OntClassImpl;
import com.hp.hpl.jena.ontology.impl.OntModelImpl;
import com.hp.hpl.jena.ontology.impl.OntPropertyImpl;

/**
 * 
 *
 * @author David Withers
 */
public class SADIActivityInputPortTest {

	private SADIActivityInputPort sadiActivityInputPort;
	
	private SADIActivity sadiActivity;
	
	private OntClass ontClass;
	
	private OntProperty ontProperty;
	
	private RDFPath rdfPath;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sadiActivity = new SADIActivity();
		OntModelImpl model = new OntModelImpl(OntModelSpec.OWL_MEM);
		ontProperty = new OntPropertyImpl(Node.createURI("/x"), model);
		ontClass = new OntClassImpl(Node.createURI("/y"), model);
		rdfPath = new RDFPath(ontProperty, ontClass);
		sadiActivityInputPort = new SADIActivityInputPort(sadiActivity, rdfPath, "port-name", SADIActivity.INPUT_DEPTH);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityInputPort#SADIActivityInputPort(net.sf.taverna.t2.activities.sadi.SADIActivity, net.sf.taverna.t2.activities.sadi.RestrictionNode, java.lang.String, int)}.
	 */
	@Test
	public void testSADIActivityInputPort() {
		/* this is dumb; if this method fails, we'll never reach this point
		 * because setUp() will have already failed...
		 * I assume this is for some sort of automated test coverage tool,
		 * though, so I'm leaving it in...
		 */
		new SADIActivityInputPort(sadiActivity, rdfPath, "port-name", 0);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityInputPort#getSADIActivity()}.
	 */
	@Test
	public void testGetSADIActivity() {
		assertEquals(sadiActivity, sadiActivityInputPort.getSADIActivity());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityInputPort#getRDFPath()}.
	 */
	@Test
	public void testGetRDFPath() {
		assertEquals(rdfPath, sadiActivityInputPort.getRDFPath());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityInputPort#getValuesFromURI()}.
	 */
	@Test
	public void testGetValuesFromURI() {
		assertEquals("/y", sadiActivityInputPort.getValuesFromURI());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityInputPort#getValuesFromLabel()}.
	 */
	@Test
	public void testGetValuesFromLabel() {
		assertEquals("y", sadiActivityInputPort.getValuesFromLabel());
	}
	
	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityInputPort#allowsLiteralValues()}.
	 */
	@Test
	public void testAllowsLiteralValues() {
		assertFalse(sadiActivityInputPort.allowsLiteralValues());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityInputPort#getHandledReferenceSchemes()}.
	 */
	@Test
	public void testGetHandledReferenceSchemes() {
		assertNull(sadiActivityInputPort.getHandledReferenceSchemes());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityInputPort#getTranslatedElementClass()}.
	 */
	@Test
	public void testGetTranslatedElementClass() {
		assertNull(sadiActivityInputPort.getTranslatedElementClass());
	}

}
