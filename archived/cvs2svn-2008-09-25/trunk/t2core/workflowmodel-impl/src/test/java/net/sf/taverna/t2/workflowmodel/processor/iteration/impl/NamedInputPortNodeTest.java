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
package net.sf.taverna.t2.workflowmodel.processor.iteration.impl;

import junit.framework.TestCase;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DiagnosticIterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;

public class NamedInputPortNodeTest extends TestCase {

	InvocationContext context = new DummyInvocationContext();

	public void testBasic() {
		NamedInputPortNode nipn = new NamedInputPortNode("Input", 0);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		nipn.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn);
		try {
			is.receiveData("Input", "Process1", new int[] {},
					DummyInvocationContext.nextReference(), context);
		} catch (WorkflowStructureException e) {
			fail("Should be able to find input named 'Input' in this test case");
		}
		assertTrue(disn.jobsReceived("Process1") == 1);
	}

	public void testMultipleProcesses() {
		NamedInputPortNode nipn = new NamedInputPortNode("Input", 0);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		nipn.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn);
		try {
			is.receiveData("Input", "Process1", new int[] { 0 },
					DummyInvocationContext.nextReference(), context);
			is.receiveData("Input", "Process1", new int[] { 1 },
					DummyInvocationContext.nextReference(), context);
			is.receiveData("Input", "Process2", new int[] {},
					DummyInvocationContext.nextReference(), context);
		} catch (WorkflowStructureException e) {
			fail("Should be able to find input named 'Input' in this test case");
		}
		assertTrue(disn.jobsReceived("Process1") == 2);
		assertTrue(disn.jobsReceived("Process2") == 1);
	}

}
