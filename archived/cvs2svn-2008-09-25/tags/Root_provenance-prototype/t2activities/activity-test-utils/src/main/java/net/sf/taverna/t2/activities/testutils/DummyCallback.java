package net.sf.taverna.t2.activities.testutils;

import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.tsunami.SecurityAgentManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorType;

/**
 * A DummyCallback to aid with testing Activities.
 * 
 * @author Stuart Owen
 * @author David Withers
 *
 */
public class DummyCallback implements AsynchronousActivityCallback {
	private final InMemoryDataManager dataManager;
	public Map<String, EntityIdentifier> data;
	public Thread thread;

	public boolean failed = false;
	
	public DummyCallback(InMemoryDataManager dataManager) {
		this.dataManager = dataManager;
	}

	public void fail(String message, Throwable t) {
		failed = true;
		throw new RuntimeException(message, t);
	}

	public void fail(String message) {
		failed = true;
		throw new RuntimeException(message);
	}

	public void fail(String message, Throwable t, DispatchErrorType arg2) {
		failed = true;
		throw new RuntimeException(message, t);
	}
	
	public DataManager getLocalDataManager() {
		return dataManager;
	}

	public SecurityAgentManager getLocalSecurityManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public void receiveCompletion(int[] completionIndex) {
		// TODO Auto-generated method stub

	}

	public void receiveResult(Map<String, EntityIdentifier> data,
			int[] index) {
		this.data = data;
	}

	public void requestRun(Runnable runMe) {
		thread = new Thread(runMe);
		thread.start();
	}

	public InvocationContext getContext() {
		return new InvocationContext() {

			public DataManager getDataManager() {
				return dataManager;
			}
			
		};
	}

	public String getParentProcessIdentifier() {
		// TODO Auto-generated method stub
		return "";
	}

}
