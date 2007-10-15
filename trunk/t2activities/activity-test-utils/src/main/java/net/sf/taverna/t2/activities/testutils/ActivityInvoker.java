package net.sf.taverna.t2.activities.testutils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;

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
	 * @throws Exception
	 */
	public static Map<String, Object> invokeAsyncActivity(
			AbstractAsynchronousActivity<?> activity,
			Map<String, Object> inputs, List<String> requestedOutputs)
			throws Exception {
		Map<String, Object> results = new HashMap<String, Object>();
		InMemoryDataManager dataManager = new InMemoryDataManager("namespace",
				new HashSet<LocationalContext>());

		DummyCallback callback = new DummyCallback(dataManager);
		DataFacade dataFacade = new DataFacade(dataManager);
		Map<String, EntityIdentifier> inputEntities = new HashMap<String, EntityIdentifier>();
		for (String inputName : inputs.keySet()) {
			Object val = inputs.get(inputName);
			inputEntities.put(inputName, dataFacade.register(val));
		}

		activity.executeAsynch(inputEntities, callback);
		callback.thread.join();
		for (String outputName : requestedOutputs) {
			EntityIdentifier id = callback.data.get(outputName);
			if (id != null) {
				results.put(outputName, dataFacade.resolve(id));
			}
		}

		return results;
	}
}
