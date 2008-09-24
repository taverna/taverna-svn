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
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DiagnosticIterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;

public class DotProductTest extends TestCase {

	InvocationContext context = new DummyInvocationContext();

	public void testBasic() {
		NamedInputPortNode nipn1 = new NamedInputPortNode("Input1", 0);
		NamedInputPortNode nipn2 = new NamedInputPortNode("Input2", 0);

		DotProduct dp = new DotProduct();
		nipn1.setParent(dp);
		nipn2.setParent(dp);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		dp.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn1);
		is.addInput(nipn2);
		try {
			is.receiveData("Input1", "Process1", new int[] { 0 },
					DummyInvocationContext.nextReference(), context);
			is.receiveCompletion("Input1", "Process1", new int[] {}, context);
			assertTrue(disn.jobsReceived("Process1") == 0);
			is.receiveData("Input2", "Process1", new int[] { 0 },
					DummyInvocationContext.nextReference(), context);
			is.receiveCompletion("Input2", "Process1", new int[] {}, context);
			assertTrue(disn.jobsReceived("Process1") == 1);
			System.out.println(disn);
		} catch (WorkflowStructureException e) {
			fail("Unknown structure exception");
		}
	}

	public void testMultipleProcess() {
		NamedInputPortNode nipn1 = new NamedInputPortNode("Input1", 0);
		NamedInputPortNode nipn2 = new NamedInputPortNode("Input2", 0);

		DotProduct dp = new DotProduct();
		nipn1.setParent(dp);
		nipn2.setParent(dp);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		dp.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn1);
		is.addInput(nipn2);
		try {
			is.receiveData("Input1", "Process1", new int[] {},
					DummyInvocationContext.nextReference(), context);
			assertTrue(disn.jobsReceived("Process1") == 0);
			is.receiveCompletion("Input1", "Process1", new int[] {}, context);
			is.receiveData("Input2", "Process2", new int[] {},
					DummyInvocationContext.nextReference(), context);
			assertTrue(disn.jobsReceived("Process1") == 0);

			is.receiveData("Input2", "Process1", new int[] {},
					DummyInvocationContext.nextReference(), context);
			assertTrue(disn.jobsReceived("Process1") == 1);
			is.receiveCompletion("Input2", "Process1", new int[] {}, context);
			System.out.println(disn);
		} catch (WorkflowStructureException e) {
			fail("Unknown structure exception");
		}
	}

	public void testMutipleData() {

		NamedInputPortNode nipn1 = new NamedInputPortNode("a", 0);
		NamedInputPortNode nipn2 = new NamedInputPortNode("b", 0);

		DotProduct dp = new DotProduct();
		nipn1.setParent(dp);
		nipn2.setParent(dp);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		dp.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn1);
		is.addInput(nipn2);

		String owningProcess = "Process1";
		for (int i = 0; i < 4; i++) {
			T2Reference listReference = DummyInvocationContext
					.nextListReference(1);

			is.receiveData("a", owningProcess, new int[] { i }, listReference,
					context);
		}
		is.receiveCompletion("a", owningProcess, new int[] {}, context);

		for (int i = 0; i < 4; i++) {

			T2Reference listReference = DummyInvocationContext
					.nextListReference(1);
			is.receiveData("b", owningProcess, new int[] { i }, listReference,
					context);
		}
		is.receiveCompletion("b", owningProcess, new int[] {}, context);
		assertTrue(disn.jobsReceived("Process1") == 4);
		System.out.println(disn);
	}

}
