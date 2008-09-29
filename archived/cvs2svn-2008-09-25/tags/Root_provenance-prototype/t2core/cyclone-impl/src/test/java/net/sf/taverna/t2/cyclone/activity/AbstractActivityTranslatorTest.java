package net.sf.taverna.t2.cyclone.activity;

import static org.junit.Assert.*;

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
