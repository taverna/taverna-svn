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
package net.sf.taverna.t2.activities.soaplab.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivityConfigurationBean;
import net.sf.taverna.t2.activities.testutils.DummyProcessor;
import net.sf.taverna.t2.activities.testutils.TranslatorTestHelper;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for SoaplabActivityTranslator.
 * 
 * @author David Withers
 */
public class SoaplabActivityTranslatorTest extends TranslatorTestHelper {

	private SoaplabActivityTranslator translator;

	private SoaplabProcessor soaplabProcessor;

	private Set<String> inputPortNames;

	private Set<String> outputPortNames;

	@Ignore("Integration test")
	@Before
	public void setUp() throws Exception {
		System.setProperty("raven.eclipse", "true");
		setUpRavenRepository();
		ScuflModel model = loadScufl("soaplab-workflow-t1.xml");
		Processor[] processors = model.getProcessors();

		assertEquals(1, processors.length);
		assertTrue(processors[0] instanceof SoaplabProcessor);
		soaplabProcessor = (SoaplabProcessor) processors[0];

		inputPortNames = new HashSet<String>();
		for (org.embl.ebi.escience.scufl.InputPort port : soaplabProcessor
				.getInputPorts()) {
			inputPortNames.add(port.getName());
		}
		outputPortNames = new HashSet<String>();
		for (org.embl.ebi.escience.scufl.OutputPort port : soaplabProcessor
				.getOutputPorts()) {
			outputPortNames.add(port.getName());
		}

		translator = new SoaplabActivityTranslator();
	}

	@Ignore("Integration test")
	@Test
	public void testCreateUnconfiguredActivity() {
		SoaplabActivity activity = translator.createUnconfiguredActivity();
		assertNotNull(activity);
		assertNull(activity.getConfiguration());
	}

	@Ignore("Integration test")
	@Test
	public void testCreateConfigTypeProcessor() throws Exception {
		SoaplabActivityConfigurationBean bean = translator
				.createConfigType(soaplabProcessor);
		assertNotNull(bean);
		assertEquals(soaplabProcessor.getEndpoint().toString(), bean
				.getEndpoint());
		assertEquals(soaplabProcessor.getPollingInterval(), bean
				.getPollingInterval());
		assertEquals(soaplabProcessor.getPollingBackoff(), bean
				.getPollingBackoff(), 0);
		assertEquals(soaplabProcessor.getPollingIntervalMax(), bean
				.getPollingIntervalMax());
	}

	@Ignore("Integration test")
	@Test
	public void testCanHandle() throws Exception {
		assertTrue(translator.canHandle(soaplabProcessor));
		assertFalse(translator.canHandle(new DummyProcessor()));
		assertFalse(translator.canHandle(null));
	}

	@Ignore("Integration test")
	@Test
	public void testDoTranslationProcessor() throws Exception {
		SoaplabActivity activity = (SoaplabActivity) translator
				.doTranslation(soaplabProcessor);
		assertEquals(soaplabProcessor.getEndpoint().toString(), activity
				.getConfiguration().getEndpoint());
		assertEquals(soaplabProcessor.getPollingInterval(), activity
				.getConfiguration().getPollingInterval());
		assertEquals(soaplabProcessor.getPollingBackoff(), activity
				.getConfiguration().getPollingBackoff(), 0);
		assertEquals(soaplabProcessor.getPollingIntervalMax(), activity
				.getConfiguration().getPollingIntervalMax());
		assertEquals(inputPortNames.size(), activity.getInputPorts().size());
		for (InputPort port : activity.getInputPorts()) {
			assertTrue(inputPortNames.remove(port.getName()));
		}
		assertEquals(outputPortNames.size(), activity.getOutputPorts().size());
		for (OutputPort port : activity.getOutputPorts()) {
			assertTrue(outputPortNames.remove(port.getName()));
		}
	}

}
