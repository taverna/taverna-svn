package net.sf.taverna.service.queue;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflModel;

public class Job {

	private static Logger logger = Logger.getLogger(Job.class);
	
	// Different from Freefluo:
	// QUEUED, DEQUEUED
	public enum State { NEW, QUEUED, DEQUEUED, RUNNING, PAUSED, FAILING, CANCELLING, CANCELLED, COMPLETE, FAILED, DESTROYED }
	
	ScuflModel workflow;
	Date created;
	State state;
	Map<String, DataThing> results;
	String progressReport;
	Map<String, DataThing> inputs;
	public final String id;

	public Job(ScuflModel workflow, Map<String, DataThing> inputs) {
		this.workflow = workflow;
		this.inputs = inputs;
		this.created = new Date();
		this.id = UUID.randomUUID().toString();
		this.state = State.NEW;
	}
	
	synchronized void setState(State state) {
		// Don't go backwards, ignore same state
		if (state.compareTo(this.state) > 0) {
			this.state = state;
			this.notifyAll();
		} else {
			logger.warn("Did not change state from " + this.state + " to " + state);
		}
	}
	
	public State getState() {
		return this.state;
	}
	
	public boolean isFinished() {
		return getState().compareTo(State.RUNNING) > 1;
	}
	
	synchronized void setResults(Map<String, DataThing> results) {
		if (! getState().equals(State.RUNNING)) {
			logger.warn("Trying to set results, but state is " + state);
			throw new IllegalStateException("Not running");
		}
		if (this.results != null) {
			throw new IllegalStateException("Already set results");
		}
		this.results = results;		
	}
	
	public synchronized Map<String, DataThing> getResults() {
		if (! getState().equals(State.COMPLETE)) {
			logger.warn("Trying to get results, but state is " + state);
			throw new IllegalStateException("Not completed");
		}
		if (this.results == null) {
			throw new IllegalStateException("Did not set results yet");
		}
		return this.results;		
	}
	
	public ScuflModel getWorkflow() {
		return workflow;
	}
	
	public Map<String, DataThing> getInputs() {
		return inputs;
	}
	
	/**
	 * Wait for the maximum specified amount of milliseconds for
	 * the job to complete.
	 * 
	 * Return the state. 
	 * 
	 * @param millis
	 * @return The state 
	 */
	public synchronized State waitForCompletion(int millis) {
		long started = new Date().getTime();		
		while (! isFinished()) {					
			long used_millis = new Date().getTime() - started;
			if (used_millis >= millis) {
				// We'll have to give up even if it might not be finished
				break;
			}
			try {				
				this.wait(millis - used_millis);
			} catch (InterruptedException e) {
				// Expected
			}
		}
		return getState();
	}

	public String getProgressReport() {
		if (progressReport == null) {
			logger.warn("Getting progress report too early");
			return "";
		}
		return progressReport;
	}

	void setProgressReport(String progressReport) {
		this.progressReport = progressReport;
	}
	
	@Override
	public String toString() {
		return "Job " + id;
	}
	
}
