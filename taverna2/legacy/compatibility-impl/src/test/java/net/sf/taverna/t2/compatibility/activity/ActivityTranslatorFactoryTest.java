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

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.compatibility.TranslatorTestHelper;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslatorFactory;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslatorNotFoundException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;
import org.embl.ebi.escience.scuflworkers.java.ByteArrayToString;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import org.junit.Test;

public class ActivityTranslatorFactoryTest extends TranslatorTestHelper {

	@Test
	public void testActivityTranslatorFactory() throws Exception {
		Processor processor = new BeanshellProcessor(null,"beanshell","",new String[]{},new String[]{});
		ActivityTranslator<?> translator = ActivityTranslatorFactory
				.getTranslator(processor);
		assertEquals("net.sf.taverna.t2.activities.beanshell.translator.BeanshellActivityTranslator", translator.getClass().getName());
	}

	@Test(expected = ActivityTranslatorNotFoundException.class)
	public void testUnknownActivityTranslator() throws Exception {
		Processor p = new DummyProcessor(); 
		ActivityTranslatorFactory.getTranslator(p);
	}

	@Test
	public void testBeanshellActivityTranslator() throws Exception {
		ScuflModel model = loadScufl("beanshell.xml");
		Processor p = model.getProcessorsOfType(BeanshellProcessor.class)[0];

		assertEquals("beanshell", p.getName());

		ActivityTranslator<?> translator = ActivityTranslatorFactory
				.getTranslator(p);

		Activity<?> s = translator.doTranslation(p);

		assertEquals("net.sf.taverna.t2.activities.beanshell.BeanshellActivity", s.getClass().getName());
	}
	
	@SuppressWarnings("serial")
	private class DummyProcessor extends BeanshellProcessor {
		public DummyProcessor() throws ProcessorCreationException, DuplicateProcessorNameException {
			super(null,"beanshell","",new String[]{},new String[]{});
		}
	};
	
	@Test
	public void testGenerateProcessorKey() throws Exception {
		Processor processor = new BeanshellProcessor(null,"beanshell","",new String[]{},new String[]{});
		assertEquals("Incorrect key","org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor",ActivityTranslatorFactory.generateProcessorKey(processor));
		
		processor = new LocalServiceProcessor(null,
				"ByteArrayToString", new ByteArrayToString());
		
		assertEquals("Incorrect key for LocalServiceProcessor","LocalServiceProcessor:org.embl.ebi.escience.scuflworkers.java.ByteArrayToString",ActivityTranslatorFactory.generateProcessorKey(processor));
	}
}
