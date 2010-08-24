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
package net.sf.taverna.t2.activities.sadi.renderers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.sadi.RDFReference;
import net.sf.taverna.t2.renderers.RendererException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Unit tests for {@link RdfNodeRenderer}.
 *
 * @author David Withers
 */
public class RdfNodeRendererTest {
	
	private RdfNodeRenderer rdfNodeRenderer;

	private ReferenceService referenceService;
	
	private T2Reference reference;

	private RDFNode rdfNode;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		rdfNodeRenderer = new RdfNodeRenderer();
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext("inMemoryActivityTestsContext.xml");
		referenceService = (ReferenceService) context.getBean("t2reference.service.referenceService");
		rdfNode = ModelFactory.createDefaultModel().createResource("http://example.com/example");
		RDFReference rdfReference = new RDFReference();
		rdfReference.setValue(rdfNode);
		reference = referenceService.register(rdfReference, 0, true, null);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.renderers.RdfNodeRenderer#canHandle(java.lang.String)}.
	 */
	@Test
	public void testCanHandleString() {
		assertFalse(rdfNodeRenderer.canHandle(null));
		assertFalse(rdfNodeRenderer.canHandle(""));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.renderers.RdfNodeRenderer#canHandle(net.sf.taverna.t2.reference.ReferenceService, net.sf.taverna.t2.reference.T2Reference, java.lang.String)}.
	 * @throws RendererException 
	 */
	@Test
	public void testCanHandleReferenceServiceT2ReferenceString() throws RendererException {
		assertTrue(rdfNodeRenderer.canHandle(referenceService, reference, null));
		assertFalse(rdfNodeRenderer.canHandle(referenceService, referenceService.register("", 0, true, null), null));
		assertFalse(rdfNodeRenderer.canHandle(referenceService, referenceService.register(new ArrayList<String>(), 1, true, null), null));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.renderers.RdfNodeRenderer#getComponent(net.sf.taverna.t2.reference.ReferenceService, net.sf.taverna.t2.reference.T2Reference)}.
	 * @throws RendererException 
	 */
	@Test
	public void testGetComponent() throws RendererException {
		assertNotNull(rdfNodeRenderer.getComponent(referenceService, reference));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.renderers.RdfNodeRenderer#getType()}.
	 */
	@Test
	public void testGetType() {
		assertEquals("RDF Node", rdfNodeRenderer.getType());
	}

}
