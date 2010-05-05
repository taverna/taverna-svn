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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

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
public class SADIActivityOutputPortTest {

	private SADIActivityOutputPort sadiActivityOutputPort;
	
	private SADIActivity sadiActivity;
	
	private RestrictionNode restrictionNode;
	
	private OntClass ontClass;
		
	private OntProperty ontProperty;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sadiActivity = new SADIActivity();
		OntModelImpl model = new OntModelImpl(OntModelSpec.OWL_MEM);
		ontProperty = new OntPropertyImpl(Node.createURI("/x"), model);
		ontClass = new OntClassImpl(Node.createURI("/y"), model);
		restrictionNode = new RestrictionNode(ontProperty, ontClass);
		sadiActivityOutputPort = new SADIActivityOutputPort(sadiActivity, restrictionNode, "port-name", 2);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityOutputPort#SADIActivityOutputPort(net.sf.taverna.t2.activities.sadi.SADIActivity, net.sf.taverna.t2.activities.sadi.RestrictionNode, java.lang.String, int)}.
	 */
	@Test
	public void testSADIActivityOutputPortSADIActivityRestrictionNodeStringInt() {
		new SADIActivityOutputPort(sadiActivity, restrictionNode, null, 0);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityOutputPort#SADIActivityOutputPort(net.sf.taverna.t2.activities.sadi.SADIActivity, net.sf.taverna.t2.activities.sadi.RestrictionNode, java.lang.String, int, int)}.
	 */
	@Test
	public void testSADIActivityOutputPortSADIActivityRestrictionNodeStringIntInt() {
		assertNotNull(new SADIActivityOutputPort(sadiActivity, restrictionNode, null, 0, 0));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityOutputPort#getSADIActivity()}.
	 */
	@Test
	public void testGetSADIActivity() {
		assertEquals(sadiActivity, sadiActivityOutputPort.getSADIActivity());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityOutputPort#getOntClass()}.
	 */
	@Test
	public void testGetOntClass() {
		assertEquals(ontClass, sadiActivityOutputPort.getOntClass());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityOutputPort#getOntProperty()}.
	 */
	@Test
	public void testGetOntProperty() {
		assertEquals(ontProperty, sadiActivityOutputPort.getOntProperty());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityOutputPort#getValues()}.
	 */
	@Test
	public void testGetValues() {
		assertNull(sadiActivityOutputPort.getValues("a"));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityOutputPort#clearValues(java.lang.String)}.
	 */
	@Test
	public void testClearValues() {
		sadiActivityOutputPort.setValues("a", Collections.singletonList("value"));
		sadiActivityOutputPort.clearValues("a");
		assertNull(sadiActivityOutputPort.getValues("a"));
	}
	
	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.SADIActivityOutputPort#setValues(java.util.List)}.
	 */
	@Test
	public void testSetValues() {
		sadiActivityOutputPort.setValues("a", Collections.singletonList("value"));
		assertEquals(Collections.singletonList("value"), sadiActivityOutputPort.getValues("a"));
		sadiActivityOutputPort.setValues("a", null);
		assertNull(sadiActivityOutputPort.getValues("a"));
	}

}
