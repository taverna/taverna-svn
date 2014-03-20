/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.reference;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.reference.impl.EmptyReferenceContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test initialization and use of the reference set dao and service
 * 
 * @author Tom Oinn
 * 
 */
public class ReferenceSetServiceTest {

	@Test
	public void doTest() {
		// Initialize application context - there's a lot going on in this one,
		// see the context definition itself as that's really the test case
		// rather than this code
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"referenceSetServiceTestContext.xml");

		// Get the reference set service object from the context. At this point
		// it will be wired and configured ready to use.
		ReferenceSetService rss = (ReferenceSetService) context
				.getBean("t2reference.service.referenceSetService");

		// Build a simple set of two external references, these have been
		// defined in the application context definition and loaded with raven,
		// the classes are not on the classpath for this test case.
		Set<ExternalReferenceSPI> references = new HashSet<ExternalReferenceSPI>();
		references
				.add((ExternalReferenceSPI) context.getBean("exampleUrlBean"));
		references.add((ExternalReferenceSPI) context
				.getBean("exampleFileBean"));

		ReferenceContext refContext = new EmptyReferenceContext();
		// If all goes well we can register the set of external references and
		// get a referenceset object back with an ID allocated appropriately
		ReferenceSet rs = rss.registerReferenceSet(references, refContext);
		System.out.println(rs);

	}
}
