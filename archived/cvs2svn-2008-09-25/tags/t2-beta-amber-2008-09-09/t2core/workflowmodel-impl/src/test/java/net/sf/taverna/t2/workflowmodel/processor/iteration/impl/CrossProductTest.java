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
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DiagnosticIterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;

import static net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext.nextReference;

public class CrossProductTest extends TestCase {

	InvocationContext context = new DummyInvocationContext();
		
	public void testBasic() {
		NamedInputPortNode nipn1 = new NamedInputPortNode("Input1", 0);
		NamedInputPortNode nipn2 = new NamedInputPortNode("Input2", 0);
		
		CrossProduct cp = new CrossProduct();
		nipn1.setParent(cp);
		nipn2.setParent(cp);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		cp.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn1);
		is.addInput(nipn2);
		try {
			is.receiveData("Input1", "Process1", new int[] {0}, nextReference(), context);
			assertTrue(disn.jobsReceived("Process1") == 0);
			is.receiveData("Input1", "Process1", new int[] {1}, nextReference(), context);
			assertTrue(disn.jobsReceived("Process1") == 0);
			is.receiveData("Input1", "Process1", new int[] {2}, nextReference(), context);
			assertTrue(disn.jobsReceived("Process1") == 0);
			is.receiveCompletion("Input1", "Process1", new int[]{}, context);
			is.receiveData("Input2", "Process1", new int[] {0}, nextReference(), context);
			System.out.println(disn);
			assertTrue(disn.jobsReceived("Process1") == 3);
			assertTrue(disn.containsJob("Process1", new int[]{0,0}));
			assertTrue(disn.containsJob("Process1", new int[]{1,0}));
			assertTrue(disn.containsJob("Process1", new int[]{2,0}));
			is.receiveData("Input2", "Process1", new int[] {1}, nextReference(), context);
			is.receiveCompletion("Input2", "Process1", new int[]{}, context);
			System.out.println(disn);
			assertTrue(disn.jobsReceived("Process1") == 6);
		} catch (WorkflowStructureException e) {
			fail("Unknown structure exception");
		}
	}
	
}
