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
import net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DiagnosticIterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.PrefixDotProduct;

public class PrefixDotProductTest extends TestCase {

	InvocationContext context = new DummyInvocationContext();

	/**
	 * Test that the prefix node copes when we feed it two inputs with different
	 * cardinalities. Output should be four jobs with index arrays length 2
	 * 
	 * @throws MalformedIdentifierException
	 * 
	 */
	public void testMutipleData() {

		NamedInputPortNode nipn1 = new NamedInputPortNode("a", 0);
		NamedInputPortNode nipn2 = new NamedInputPortNode("b", 0);

		PrefixDotProduct pdp = new PrefixDotProduct();
		nipn1.setParent(pdp);
		nipn2.setParent(pdp);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		pdp.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn1);
		is.addInput(nipn2);

		String owningProcess = "Process1";
		for (int i = 0; i < 2; i++) {
			is.receiveData("a", owningProcess, new int[] { i },
					DummyInvocationContext.nextListReference(1), context);
		}
		is.receiveCompletion("a", owningProcess, new int[] {}, context);

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				is.receiveData("b", owningProcess, new int[] { i, j },
						DummyInvocationContext.nextListReference(1), context);
			}
			is.receiveCompletion("b", owningProcess, new int[] { i }, context);
		}
		is.receiveCompletion("b", owningProcess, new int[] {}, context);
		assertTrue(disn.jobsReceived("Process1") == 4);
		System.out.println(disn);
	}

}
