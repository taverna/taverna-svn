package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import static net.sf.taverna.t2.workflowmodel.processor.iteration.impl.CrossProductTest.nextID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;
import net.sf.taverna.t2.workflowmodel.processor.service.ServiceAnnotationContainer;

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

	public void receiveJob(Job job, List<? extends ServiceAnnotationContainer> services) {
		final Job j = job;
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(400);
					for (int i = 0; i < 4; i++) {
						Map<String, EntityIdentifier> dataMap = new HashMap<String, EntityIdentifier>();
						dataMap.put("Result1", nextID());
						int[] newIndex = new int[j.getIndex().length + 1];
						for (int k = 0; k < j.getIndex().length; k++) {
							newIndex[k] = j.getIndex()[k];
						}
						newIndex[j.getIndex().length] = i;
						getAbove()
								.receiveResult(
										new Job(j.getOwningProcess(), newIndex,
												dataMap));
						Thread.sleep(200);
					}
					getAbove().receiveResultCompletion(
							new Completion(j.getOwningProcess(), j
									.getIndex()));
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
