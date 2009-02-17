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
package net.sf.taverna.t2.compatibility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.compatibility.WorkflowModelTranslator;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.junit.Test;

public class PortBindingTest extends TranslatorTestHelper {

	@SuppressWarnings("null")
	@Test
	public void testRemovalOfUnboundPorts() throws Exception {
		Dataflow dataflow = loadAndTranslateWorkflow("unbound_ports.xml");
		Processor processor_A=null;
		Processor processor_B=null;
		
		assertEquals("there should only be 2 processors",2,dataflow.getProcessors().size());
		for (Processor p : dataflow.getProcessors()) {
			if (p.getLocalName().equals("Processor_A")) processor_A=p;
			if (p.getLocalName().equals("Processor_B")) processor_B=p;
		}
		
		assertNotNull("Could not find processor A",processor_A);
		assertNotNull("Could not find processor B",processor_B);
		
		assertEquals("processor_A should have no inputs",0,processor_A.getInputPorts().size());
		assertEquals("processor_A should have 1 output",1,processor_A.getOutputPorts().size());
		assertEquals("processor_B should have 1 input",1,processor_B.getInputPorts().size());
		assertEquals("processor_B should have 1 input named input_1","input_1",processor_B.getInputPorts().get(0).getName());
		assertEquals("processor_B should have 1 output",1,processor_B.getOutputPorts().size());
		
		assertEquals("processor_B should have 1 activity",1,processor_B.getActivityList().size());
		Activity<?>activity = processor_B.getActivityList().get(0);
		
		assertEquals("activity should have 3 input ports",3,activity.getInputPorts().size());
	}
	
	@SuppressWarnings("null")
	@Test
	public void testReplaceDefaultWithStringConstant() throws Exception {
		Dataflow dataflow = loadAndTranslateWorkflow("port_with_default.xml");
		Processor processor_A = null;
		Processor processorStringConstant = null;
		
		assertEquals("There should be 2 processors",2,dataflow.getProcessors().size());
		for (Processor p : dataflow.getProcessors()) {
			if (p.getLocalName().equals("Processor_A")) {
				processor_A = p;
			}
			else {
				processorStringConstant = p;
			}
		}
		
		assertNotNull("There should be a processor named processor_A",processor_A);
		assertEquals("processor_A should have 1 input",1,processor_A.getInputPorts().size());
		assertEquals("The should only be 1 activity",1,processorStringConstant.getActivityList().size());
		assertEquals("Processor_A should have 1 iteration stragegy",1,processor_A.getIterationStrategy().getStrategies().size());
		
		assertEquals("There should be a processor that contains a string constant activity",StringConstantActivity.class,processorStringConstant.getActivityList().get(0).getClass());
		
		StringConstantActivity activity = (StringConstantActivity) processorStringConstant.getActivityList().get(0);
		assertEquals("activity should have value 'Some Data'","Some Data",activity.getStringValue());
		
		assertEquals("The string constant shoudl have 1 output port",1,processorStringConstant.getOutputPorts().size());
		assertEquals("There should only be 1 outgoing link",1,processorStringConstant.getOutputPorts().get(0).getOutgoingLinks().size());
		
		Datalink link = (Datalink)processorStringConstant.getOutputPorts().get(0).getOutgoingLinks().toArray()[0];
		assertEquals("it should be linked to processor_A",processor_A.getInputPorts().get(0),link.getSink());
		
	}
	
	@SuppressWarnings("null")
	@Test
	public void testDefaultAndUnboundMixed() throws Exception {
		Dataflow dataflow = loadAndTranslateWorkflow("unbound_ports_with_default.xml");
		Processor processor_A = null;
		Processor processorStringConstant = null;
		
		assertEquals("There should be 2 processors",2,dataflow.getProcessors().size());
		for (Processor p : dataflow.getProcessors()) {
			if (p.getLocalName().equals("Processor_A")) {
				processor_A = p;
			}
			else {
				processorStringConstant = p;
			}
		}
		
		assertNotNull("There should be a processor named processor_A",processor_A);
		assertEquals("processor_A should have 1 input",1,processor_A.getInputPorts().size());
		assertEquals("The should only be 1 activity",1,processorStringConstant.getActivityList().size());
		assertEquals("There should be a processor that contains a string constant activity",StringConstantActivity.class,processorStringConstant.getActivityList().get(0).getClass());
		
		StringConstantActivity activity = (StringConstantActivity) processorStringConstant.getActivityList().get(0);
		assertEquals("activity should have value 'Some Data'","Some Data",activity.getStringValue());
		
		assertEquals("The string constant shoudl have 1 output port",1,processorStringConstant.getOutputPorts().size());
		assertEquals("There should only be 1 outgoing link",1,processorStringConstant.getOutputPorts().get(0).getOutgoingLinks().size());
		
		Datalink link = (Datalink)processorStringConstant.getOutputPorts().get(0).getOutgoingLinks().toArray()[0];
		assertEquals("it should be linked to processor_A",processor_A.getInputPorts().get(0),link.getSink());
	}
	
	@SuppressWarnings("null")
	@Test
	public void testBoundPortOverridesDefault() throws Exception {
		Dataflow dataflow = loadAndTranslateWorkflow("bound_port_overides_default.xml");
		assertEquals("there should only be 2 processors",2,dataflow.getProcessors().size());
		
		Processor processor_A=null;
		Processor processor_B=null;
		
		for (Processor p : dataflow.getProcessors()) {
			if (p.getLocalName().equals("Processor_A")) processor_A=p;
			if (p.getLocalName().equals("Processor_B")) processor_B=p;
		}
		
		assertNotNull("Could not find processor A",processor_A);
		assertNotNull("Could not find processor B",processor_B);
		
		assertEquals("processor_A should have no inputs",0,processor_A.getInputPorts().size());
		assertEquals("processor_A should have 1 output",1,processor_A.getOutputPorts().size());
		assertEquals("processor_B should have 1 input",1,processor_B.getInputPorts().size());
		assertEquals("processor_B should have 1 input named input_1","input_1",processor_B.getInputPorts().get(0).getName());
		assertEquals("processor_B should have 1 output",1,processor_B.getOutputPorts().size());
		
		assertEquals("processor_B should have 1 activity",1,processor_B.getActivityList().size());
		Activity<?>activity = processor_B.getActivityList().get(0);
		
		assertEquals("activity should have 3 input ports",3,activity.getInputPorts().size());
	}
	
	private Dataflow loadAndTranslateWorkflow(String resourceName) throws Exception {
		ScuflModel model = loadScufl(resourceName);
		return WorkflowModelTranslator.doTranslation(model);
	}
	
	/**
	 * A default port is converted into an upstream StringConstant, whose name is based upon
	 * the original port name. However, its possible to name a port with a char that is invalid for
	 * a processor, e.g '-'. This test checks that this is handled by stripping out the offending character.
	 * @throws Exception
	 */
	@SuppressWarnings("null")
	@Test
	public void testInvalidDefaultPortName() throws Exception {
		Dataflow dataflow = loadAndTranslateWorkflow("port_with_badly_named_default.xml");
		Processor processor_A = null;
		Processor processorStringConstant = null;
		
		assertEquals("There should be 2 processors",2,dataflow.getProcessors().size());
		for (Processor p : dataflow.getProcessors()) {
			if (p.getLocalName().equals("Processor_A")) {
				processor_A = p;
			}
			else {
				processorStringConstant = p;
			}
		}
		
		assertNotNull("There should be a processor named processor_A",processor_A);
		assertNotNull("There should be a string constant processor",processorStringConstant);
		assertEquals("The String Constant processor is named incorrectly","Processor_A_input_defaultValue",processorStringConstant.getLocalName());
	}
}
