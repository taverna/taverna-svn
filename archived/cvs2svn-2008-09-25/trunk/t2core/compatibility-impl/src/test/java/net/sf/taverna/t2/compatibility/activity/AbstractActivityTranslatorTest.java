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
package net.sf.taverna.t2.compatibility.activity;

import static org.junit.Assert.*;

import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;
import org.junit.Before;
import org.junit.Test;

public class AbstractActivityTranslatorTest {

	private AbstractActivityTranslator<Object> abstractActivityTranslator;
	
	@Before
	public void setUp() throws Exception {
		abstractActivityTranslator = new AbstractActivityTranslator<Object>() {

			@Override
			protected Object createConfigType(Processor processor)
					throws ActivityTranslationException {
				return null;
			}

			@Override
			protected Activity<Object> createUnconfiguredActivity() {
				return null;
			}

			public boolean canHandle(Processor processor) {
				return false;
			}
			
		};
	}

	@Test
	public void testDetermineClassFromSyntacticType() {
		assertEquals(String.class, abstractActivityTranslator.determineClassFromSyntacticType("'text/plain'"));
		assertEquals(String.class, abstractActivityTranslator.determineClassFromSyntacticType("l('text/plain')"));
		assertEquals(String.class, abstractActivityTranslator.determineClassFromSyntacticType("l(l('text/plain'))"));
		assertEquals(String.class, abstractActivityTranslator.determineClassFromSyntacticType("l('text/plain')"));
		assertEquals(String.class, abstractActivityTranslator.determineClassFromSyntacticType("l(l('text/plain'))"));
		assertEquals(String.class, abstractActivityTranslator.determineClassFromSyntacticType("chemical/x-swissprot"));
		assertEquals(byte[].class, abstractActivityTranslator.determineClassFromSyntacticType("application/zip"));
		assertEquals(byte[].class, abstractActivityTranslator.determineClassFromSyntacticType("image/jpeg"));
		assertEquals(byte[].class, abstractActivityTranslator.determineClassFromSyntacticType("l('image/gif')"));
		assertEquals(byte[].class, abstractActivityTranslator.determineClassFromSyntacticType("l()"));
		assertEquals(byte[].class, abstractActivityTranslator.determineClassFromSyntacticType("l('')"));
		assertEquals(byte[].class, abstractActivityTranslator.determineClassFromSyntacticType("''"));
	}

}
