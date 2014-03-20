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
 * Test initialization and use of the reference set augmentor implementation
 * 
 * @author Tom Oinn
 */
public class ReferenceSetAugmentorTest {

	@SuppressWarnings("unchecked")
	@Test
	public void doTest() {
		// Initialize application context - there's a lot going on in this one,
		// see the context definition itself as that's really the test case
		// rather than this code
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"referenceSetAugmentorTestContext.xml");

		// Get the pre-baked reference set
		ReferenceSet rs = (ReferenceSet) context.getBean("referenceSet");

		ReferenceSetAugmentor aug = (ReferenceSetAugmentor) context
				.getBean("t2reference.augmentor");

		ReferenceContext refContext = new EmptyReferenceContext();

		Set<Class<ExternalReferenceSPI>> redTarget = new HashSet<Class<ExternalReferenceSPI>>();
		redTarget.add((Class<ExternalReferenceSPI>) context.getBean("redBean")
				.getClass());

		aug.augmentReferenceSet(rs, redTarget, refContext);

	}

	
}
