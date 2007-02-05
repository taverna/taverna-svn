package net.sf.taverna.service.queue;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflModel;

public class Job {

	// Different from Freefluo:
	// QUEUED, DEQUEUED
	public enum State { NEW, QUEUED, DEQUEUED, RUNNING, PAUSED, FAILING, FAILED, CANCELLING, CANCELLED, COMPLETE, DESTROYED }
	
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
	
	public synchronized void setState(State state) {
		if (state.compareTo(this.state) > 0) {
			this.state = state;
			this.notifyAll();
		} 
	}
	
	public State getState() {
		return this.state;
	}
	
	public boolean isFinished() {
		return getState().compareTo(State.RUNNING) > 1;
	}
	
	public void setResults(Map<String, DataThing> results) {
		if (! getState().equals(State.RUNNING)) {
			throw new IllegalStateException("Not running");
		}
		this.results = results;		
	}
	
	public Map<String, DataThing> getResults() {
		if (! getState().equals(State.COMPLETE)) {
			throw new IllegalStateException("Not completed");
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
		if (! isFinished()) {
			throw new IllegalStateException("Not finished");
		}
		return progressReport;
	}

	public void setProgressReport(String progressReport) {
		if (! getState().equals(State.RUNNING)) {
			throw new IllegalStateException("Not running");
		}
		this.progressReport = progressReport;
	}
	
	
}
