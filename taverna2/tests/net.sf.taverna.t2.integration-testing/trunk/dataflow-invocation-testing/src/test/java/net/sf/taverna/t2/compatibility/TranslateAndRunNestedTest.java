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
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.testing.CaptureResultsListener;
import net.sf.taverna.t2.testing.InvocationTestHelper;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.junit.Test;

/**
 * Tests specifically associated with Nested Workflows.
 * @author Stuart Owen
 *
 */
public class TranslateAndRunNestedTest extends InvocationTestHelper {
	
	@SuppressWarnings("unchecked")
	@Test
	public void simpleNested() throws Exception {
		Dataflow dataflow = translateScuflFile("simple-nested-test.xml");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		WorkflowInstanceFacade facade;
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,context);
		facade.addResultListener(listener);
		
		facade.fire();
		
		waitForCompletion(listener);
		
		
		assertNotNull("There should have been an output event handler named 'out'",listener.getResult("out"));
		assertTrue("There result should be a list",listener.getResult("out") instanceof List);
		List<String> result = (List<String>)listener.getResult("out");
		assertEquals("one-x",result.get(0));
		assertEquals("two-x",result.get(1));
		assertEquals("three-x",result.get(2));
		assertEquals("four-x",result.get(3));
		assertEquals("five-x",result.get(4));
	}
	
	/**
	 * A workflow that contains a Nested Workflow which recieves a List of Lists.
	 * Internally the first processor in the nested workflow requires a single item
	 * causing the nested workflow itself to produce a list of lists. 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void lessSimpleNestedIteratesListOfLists() throws Exception {
		Dataflow dataflow = translateScuflFile("less-simple-nested-test.xml");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());

		WorkflowInstanceFacade facade;
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,context);
		facade.addResultListener(listener);
		
		facade.fire();
		
		waitForCompletion(listener);
		
		assertNotNull("There should have been an output event handler named 'out'",listener.getResult("out"));
		assertTrue("There result should be a list",listener.getResult("out") instanceof List);
		List<List> result = (List<List>)listener.getResult("out");
		assertTrue("The result should be a list of lists",result.get(0) instanceof List);
		assertEquals("There should be 3 lists within the results",3,result.size());
		for (List innerList : result) {
			assertEquals("There should be 5 elements within each inner list",5,innerList.size());
			String [] expectedResults = new String[] {"one-xxx","two-xxx","three-xxx","four-xxx","five-xxx"};
			int i=0;
			for (Object innerListItem : innerList) {
				assertEquals(expectedResults[i++],innerListItem);
			}
		}
	}
	
	/**
	 * As in lessSimpleNestedIteratesListOfLists except the processor consumes a list and produces a single item.
	 * This causes the nested workflow to produce a list of 3 string items (which contains the a concatenated
	 * string referring to the 5 items in the orginal inner list).
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void lessSimpleNestedIteratesListOfLists2() throws Exception {
		Dataflow dataflow = translateScuflFile("less-simple-nested-test2.xml");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());

		WorkflowInstanceFacade facade;
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,context);
		facade.addResultListener(listener);
		
		facade.fire();
		
		waitForCompletion(listener);
		
		assertNotNull("There should have been an output event handler named 'out'",listener.getResult("out"));
		assertTrue("There result should be a list",listener.getResult("out") instanceof List);
		List<String> result = (List<String>)listener.getResult("out");
		assertEquals("There should be 3 lists within the results",3,result.size());
		assertTrue("The result should be a list of lists",result.get(0) instanceof String);
		for (String resultItem : result) {
			assertEquals("Unexpected String in the result","[one, two, three, four, five]-xxx",resultItem);
		}
	}
}
