package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

/**
 * Bean to hold the configuration for the parallelize layer, specifically a
 * single int property defining the number of concurrent jobs in that processor
 * instance per owning process ID.
 * 
 * @author Tom Oinn
 * 
 */
public class ParallelizeConfig {

	private int maxJobs;

	public ParallelizeConfig() {
		super();
		this.maxJobs = 5;
	}

	public void setMaximumJobs(int maxJobs) {
		this.maxJobs = maxJobs;
	}

	public int getMaximumJobs() {
		return this.maxJobs;
	}

}
