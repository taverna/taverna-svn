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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import javax.swing.tree.DefaultMutableTreeNode;

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
 * Unit tests for {@link RestrictionNode}.
 *
 * @author David Withers
 */
public class RestrictionNodeTest {

	private OntModelImpl model;
	private OntProperty ontProperty;
	private OntClass ontClass;
	private RestrictionNode restrictedProperty, parentRestrictedProperty;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		model = new OntModelImpl(OntModelSpec.OWL_MEM);
		ontProperty = new OntPropertyImpl(Node.createURI("/x"), model);
		ontClass = new OntClassImpl(Node.createURI("/y"), model);
		restrictedProperty = new RestrictionNode(ontProperty, ontClass);
		parentRestrictedProperty = new RestrictionNode(ontProperty, ontClass);
		parentRestrictedProperty.add(restrictedProperty);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#RestrictionNode(com.hp.hpl.jena.ontology.OntClass)}.
	 */
	@Test
	public void testRestrictionNodeOntClass() {
		restrictedProperty = new RestrictionNode(ontClass);
		assertNull(restrictedProperty.getOntProperty());
		assertEquals(ontClass, restrictedProperty.getOntClass());
		assertFalse(restrictedProperty.isExclusive());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRestrictionNodeOntClassException() {
		restrictedProperty = new RestrictionNode(null);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#RestrictionNode(com.hp.hpl.jena.ontology.OntProperty, com.hp.hpl.jena.ontology.OntClass)}.
	 */
	@Test
	public void testRestrictionNodeOntPropertyOntClass() {
		restrictedProperty = new RestrictionNode(ontProperty, ontClass);
		assertEquals(ontProperty, restrictedProperty.getOntProperty());
		assertEquals(ontClass, restrictedProperty.getOntClass());
		assertFalse(restrictedProperty.isExclusive());
		restrictedProperty = new RestrictionNode(null, ontClass);
		assertNull(restrictedProperty.getOntProperty());
		assertNotNull(restrictedProperty.getOntClass());
		assertFalse(restrictedProperty.isExclusive());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#RestrictionNode(com.hp.hpl.jena.ontology.OntProperty, com.hp.hpl.jena.ontology.OntClass, boolean)}.
	 */
	@Test
	public void testRestrictionNodeOntPropertyOntClassBoolean() {
		restrictedProperty = new RestrictionNode(ontProperty, ontClass, true);
		assertEquals(ontProperty, restrictedProperty.getOntProperty());
		assertEquals(ontClass, restrictedProperty.getOntClass());
		assertTrue(restrictedProperty.isExclusive());
		restrictedProperty = new RestrictionNode(null, ontClass, false);
		assertNull(restrictedProperty.getOntProperty());
		assertNotNull(restrictedProperty.getOntClass());
		assertFalse(restrictedProperty.isExclusive());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#getOntProperty()}.
	 */
	@Test
	public void testGetOntProperty() {
		assertEquals(ontProperty, restrictedProperty.getOntProperty());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#getOntClass()}.
	 */
	@Test
	public void testGetOntClass() {
		assertEquals(ontClass, restrictedProperty.getOntClass());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#isExclusive()}.
	 */
	@Test
	public void testIsExclusive() {
		assertFalse(restrictedProperty.isExclusive());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#isSelected()}.
	 */
	@Test
	public void testIsSelected() {
		assertFalse(restrictedProperty.isSelected());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#setSelected()}.
	 */
	@Test
	public void testSetSelected() {
		assertFalse(parentRestrictedProperty.isSelected());
		assertFalse(restrictedProperty.isSelected());
		restrictedProperty.setSelected();
		assertTrue(parentRestrictedProperty.isSelected());
		assertTrue(restrictedProperty.isSelected());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#clearSelected()}.
	 */
	@Test
	public void testClearSelected() {
		restrictedProperty.setSelected();
		parentRestrictedProperty.clearSelected();
		assertFalse(parentRestrictedProperty.isSelected());
		assertFalse(restrictedProperty.isSelected());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#getValues(java.lang.String)}.
	 */
	@Test
	public void testGetValues() {
		assertNull(restrictedProperty.getValues("id"));
		assertNull(restrictedProperty.getValues(""));
		assertNull(restrictedProperty.getValues(null));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#setValues(java.lang.String, java.util.List)}.
	 */
	@Test
	public void testSetValues() {
		restrictedProperty.setValues("id", Collections.singletonList("listItem1"));
		assertEquals(Collections.singletonList("listItem1"), restrictedProperty.getValues("id"));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#clearValues(java.lang.String)}.
	 */
	@Test
	public void testClearValues() {
		restrictedProperty.setValues("id", Collections.singletonList("listItem1"));
		assertEquals(Collections.singletonList("listItem1"), restrictedProperty.getValues("id"));
		restrictedProperty.clearValues("");
		assertEquals(Collections.singletonList("listItem1"), restrictedProperty.getValues("id"));
		restrictedProperty.clearValues(null);
		assertEquals(Collections.singletonList("listItem1"), restrictedProperty.getValues("id"));
		restrictedProperty.clearValues("id");
		assertNull(restrictedProperty.getValues("id"));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#getChildren()}.
	 */
	@Test
	public void testGetChildren() {
		assertEquals(Collections.singletonList(restrictedProperty), parentRestrictedProperty.getChildren());
		parentRestrictedProperty.add(new DefaultMutableTreeNode());
		assertEquals(Collections.singletonList(restrictedProperty), parentRestrictedProperty.getChildren());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.RestrictionNode#toString()}.
	 */
	@Test
	public void testToString() {
		assertEquals("x (y)", restrictedProperty.toString());
		restrictedProperty = new RestrictionNode(ontClass);
		assertEquals("y", restrictedProperty.toString());
	}

}
