package net.sf.taverna.t2.activities.testutils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;

import org.springframework.context.ApplicationContext;

/**
 * Helper class to facilitate in executing Activities in isolation.
 * 
 * @author Stuart Owen
 * 
 */
public class ActivityInvoker {

	/**
	 * Invokes an {@link AsynchronousActivity} with a given set of input Objects
	 * and returns a Map<String,Object> of requested output values.
	 * 
	 * @param activity
	 *            the activity to be tested
	 * @param inputs
	 *            a Map<String,Object> of input Objects
	 * @param requestedOutputs
	 *            a List<String> of outputs to be examined
	 * 
	 * @return a Map<String,Object> of the outputs requested by requestedOutput
	 *         or <code>null</code> if a failure occurs
	 * @throws Exception
	 */
	public static Map<String, Object> invokeAsyncActivity(
			AbstractAsynchronousActivity<?> activity,
			Map<String, Object> inputs, Collection<String> requestedOutputs)
			throws Exception {
		Map<String, Object> results = new HashMap<String, Object>();

		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
		"inMemoryReferenceServiceTestContext.xml");
		ReferenceService referenceService = (ReferenceService) context.getBean("t2reference.service.referenceService");

		DummyCallback callback = new DummyCallback(referenceService);
		Map<String, T2Reference> inputEntities = new HashMap<String, T2Reference>();
		for (String inputName : inputs.keySet()) {
			Object val = inputs.get(inputName);
			inputEntities.put(inputName, referenceService.register(val, 0, true, callback.getContext()));
		}

		activity.executeAsynch(inputEntities, callback);
		callback.thread.join();

		if (callback.failed) {
			results = null;
		} else {
			for (String outputName : requestedOutputs) {
				T2Reference id = callback.data.get(outputName);
				if (id != null) {
					Object result;
					try {
						result = referenceService.renderIdentifier(id, String.class, callback.getContext());
					} catch (ReferenceServiceException e) {
						result = referenceService.renderIdentifier(id, byte[].class, callback.getContext());
					}

//					if (result instanceof ByteArrayInputStream) {
//						result = IOUtils.toByteArray((ByteArrayInputStream) result);
//					}
					results.put(outputName, result);
				}
			}
		}
		return results;
	}
}
