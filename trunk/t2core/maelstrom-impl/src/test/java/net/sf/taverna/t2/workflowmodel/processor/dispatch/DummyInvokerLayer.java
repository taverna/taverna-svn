package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;
import net.sf.taverna.t2.workflowmodel.processor.service.ServiceAnnotationContainer;
import static net.sf.taverna.t2.workflowmodel.processor.iteration.impl.CrossProductTest.nextID;

/**
 * Test dispatch layer which acts as a fake invoker. Receives jobs sent to it
 * and always sends a result back after a delay of 400ms. The result is non
 * streaming and therefore always has the same index array as the job submitted.
 * 
 * @author Tom
 * 
 */
public class DummyInvokerLayer extends AbstractDispatchLayer<Object> {

	public void receiveJob(Job job, List<? extends ServiceAnnotationContainer> services) {
		final Job j = job;
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(400);
					Map<String, EntityIdentifier> dataMap = new HashMap<String, EntityIdentifier>();
					dataMap.put("Result1", nextID());
					getAbove().receiveResult(
							new Job(j.getOwningProcess(), j.getIndex(),
									dataMap));
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
