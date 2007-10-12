package net.sf.taverna.t2.activities.testutils;

import java.util.Map;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.tsunami.SecurityAgentManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

/**
 * A DummyCallback to aid with testing Activities.
 * 
 * @author Stuart Owen
 *
 */
public class DummyCallback implements AsynchronousActivityCallback {
	private final InMemoryDataManager dataManager;
	public Map<String, EntityIdentifier> data;
	public Thread thread;

	public DummyCallback(InMemoryDataManager dataManager) {
		this.dataManager = dataManager;
	}

	public void fail(String message, Throwable t) {
		// TODO Auto-generated method stub

	}

	public void fail(String message) {
		// TODO Auto-generated method stub

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
}
