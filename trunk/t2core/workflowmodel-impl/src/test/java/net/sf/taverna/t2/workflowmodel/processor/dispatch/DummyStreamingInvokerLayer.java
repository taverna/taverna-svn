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
package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchCompletionEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchResultEvent;

/**
 * Acts as a fake invocation layer as with the DummyInvokerLayer but this one
 * 'streams' partial results back followed by a completion event. Data is
 * started after a 400ms delay and four items are returned each with a 200ms
 * delay inbetween them followed immediately by a completion event.
 * 
 * @author Tom
 * 
 */
public class DummyStreamingInvokerLayer extends AbstractDispatchLayer<Object> {

	public void receiveJob(Job job, List<? extends Activity<?>> activities) {
		final Job j = job;
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(400);
					for (int i = 0; i < 4; i++) {
						Map<String, T2Reference> dataMap = new HashMap<String, T2Reference>();
						dataMap.put("Result1", DummyInvocationContext.nextReference());
						int[] newIndex = new int[j.getIndex().length + 1];
						for (int k = 0; k < j.getIndex().length; k++) {
							newIndex[k] = j.getIndex()[k];
						}
						newIndex[j.getIndex().length] = i;
						getAbove()
								.receiveResult(
										new DispatchResultEvent(j.getOwningProcess(), newIndex,
												j.getContext(), dataMap, true));
						Thread.sleep(200);
					}
					getAbove().receiveResultCompletion(
							new DispatchCompletionEvent(j.getOwningProcess(), j
									.getIndex(), j.getContext()));
				} catch (InterruptedException ie) {
					//
				}
			}

		}).start();
	}

	public void configure(Object config) {
		// Do nothing		
	}

	public Object getConfiguration() {
		return null;
	}

}
