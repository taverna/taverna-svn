/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: ModelTranslatorTest.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-09-17 09:29:26 $
 *               by   $Author: sowen70 $
 * Created on Sep 7, 2007
 *****************************************************************/
package net.sf.taverna.t2.cyclone;

import static org.junit.Assert.assertTrue; //
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.junit.Test;

/**
 * @author David Withers
 * @author Stuart Owen
 * 
 */
public class ModelTranslatorTest extends TranslatorTestHelper {

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.cyclone.WorkflowModelTranslator#doTranslation(org.embl.ebi.escience.scufl.ScuflModel)}.
	 * 
	 * @throws IOException
	 * @throws XScuflFormatException
	 * @throws DuplicateConcurrencyConstraintNameException
	 * @throws ConcurrencyConstraintCreationException
	 * @throws MalformedNameException
	 * @throws DuplicateProcessorNameException
	 * @throws DataConstraintCreationException
	 * @throws ProcessorCreationException
	 * @throws UnknownPortException
	 * @throws UnknownProcessorException
	 */
	@Test
	public void testDoTranslation() throws UnknownProcessorException,
			UnknownPortException, ProcessorCreationException,
			DataConstraintCreationException, DuplicateProcessorNameException,
			MalformedNameException, ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException,
			IOException {

		boolean runTest = false; // this will be removed once the test is
									// working. It currently relies on skeleton
									// code being implemented. This flag allows
									// code to be committed during this process
									// without the need to keep
									// commenting/uncommenting this test.

		if (runTest) {

			ScuflModel model = loadScufl("translation-test.xml");
			Dataflow dataflow = WorkflowModelTranslator.doTranslation(model);

			List<String> processorNames = Arrays.asList("processor_a",
					"processor_b");
			List<String> portNames = Arrays.asList("input_1", "input_1",
					"input_2", "output_1", "output_1", "output_2");

			for (Processor processor : dataflow.getProcessors()) {
				assertTrue(processorNames.remove(processor.getLocalName()));
				for (Port inputPort : processor.getInputPorts()) {
					assertTrue(portNames.remove(inputPort.getName()));
				}
				for (Port outputPort : processor.getOutputPorts()) {
					assertTrue(portNames.remove(outputPort.getName()));
				}
			}
			assertTrue(portNames.isEmpty());
			assertTrue(processorNames.isEmpty());

			for (Datalink datalink : dataflow.getLinks()) {
				datalink.getSource();
			}
		}

		return;

	}

}
