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
package net.sf.taverna.t2.activities.dataflow.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.testutils.DummyProcessor;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor;
import org.junit.Before;
import org.junit.Test;

/**
 * Dataflow Activity Translator Tests
 * 
 * @author David Withers
 * 
 */
public class DataflowActivityTranslatorTest {

	private DataflowActivityTranslator translator;

	private WorkflowProcessor processor;

	@Before
	public void setUp() throws Exception {
		translator = new DataflowActivityTranslator();
		ScuflModel scuflModel = new ScuflModel();
		processor = new WorkflowProcessor(scuflModel, "test");
	}

	@Test
	public void testCreateUnconfiguredActivity() {
		DataflowActivity activity = translator.createUnconfiguredActivity();
		assertNotNull(activity);
		assertNull(activity.getConfiguration());
	}

	@Test
	public void testCreateConfigTypeProcessor() throws Exception {
		Dataflow df = translator
				.createConfigType(processor);
		assertNotNull(df);
	}

	@Test
	public void testCanHandle() throws Exception {
		assertTrue(translator.canHandle(processor));
		assertFalse(translator.canHandle(new DummyProcessor()));
		assertFalse(translator.canHandle(null));
	}

	@Test
	public void testDoTranslationProcessor() throws Exception {
		DataflowActivity activity = (DataflowActivity) translator
				.doTranslation(processor);
		assertEquals(0, activity.getInputPorts().size());
		assertEquals(0, activity.getOutputPorts().size());
	}

}
