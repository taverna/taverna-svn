package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.invocation.impl.TestInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchResultEvent;

/**
 * Test dispatch layer which acts as a fake invoker. Receives jobs sent to it
 * and always sends a result back after a delay of 400ms. The result is non
 * streaming and therefore always has the same index array as the job submitted.
 * 
 * @author Tom
 * 
 */
public class DummyInvokerLayer extends AbstractDispatchLayer<Object> {

	public void receiveJob(final Job job, List<? extends Activity<?>> activities) {
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(400);
					Map<String, T2Reference> dataMap = new HashMap<String, T2Reference>();
					dataMap.put("Result1", TestInvocationContext.nextReference());
					getAbove().receiveResult(
							new DispatchResultEvent(job.getOwningProcess(),
									job.getIndex(), job.getContext(), dataMap,
									false));
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
