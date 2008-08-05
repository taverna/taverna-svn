package net.sf.taverna.t2.activities.testutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.security.SecurityAgentManager;
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
	
	public InvocationContext invocationContext = new InvocationContext() {
		public <T> List<? extends T> getEntities(Class<T> arg0) {
			return new ArrayList<T>();
		}
		
		public ReferenceService getReferenceService() {
			return referenceService;
		}
	};
	public ReferenceService referenceService;
	public Map<String, T2Reference> data;
	public Thread thread;

	public boolean failed = false;
	
	public DummyCallback(ReferenceService referenceService) {
		this.referenceService = referenceService;
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
	
	/*public SecurityAgentManager getLocalSecurityManager() {
		// TODO Auto-generated method stub
		return null;
	}*/

	public void receiveCompletion(int[] completionIndex) {
		// TODO Auto-generated method stub

	}

	public void receiveResult(Map<String, T2Reference> data,
			int[] index) {
		this.data = data;
	}

	public void requestRun(Runnable runMe) {
		thread = new Thread(runMe);
		thread.start();
	}

	public InvocationContext getContext() {
		return invocationContext;
	}

	public String getParentProcessIdentifier() {
		// TODO Auto-generated method stub
		return "";
	}

}
