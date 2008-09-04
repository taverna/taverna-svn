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
package net.sf.taverna.t2.workflowmodel.processor;

import static net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext.nextReference;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;

public class JobTest extends TestCase {

	InvocationContext context = new DummyInvocationContext();
	
	public void testPopPush() {
		Map<String,T2Reference> dataMap = new HashMap<String,T2Reference>();
		dataMap.put("Key1",nextReference());
		dataMap.put("Key2",nextReference());
		Job j = new Job("Process1",new int[]{1,0}, dataMap, context);
		// Check that push / pop returns equal to the original Job
		assertTrue(j.toString().equals(j.pushIndex().popIndex().toString()));
		// Index array of pushed job is always zero
		assertTrue(j.pushIndex().getIndex().length == 0);
	}
	
}
