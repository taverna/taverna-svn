package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

public class ProvenanceConfig {
	
	private int maxJobs;
	
	public ProvenanceConfig(){
		this.maxJobs = 5;
	}

	public int getMaxJobs() {
		return maxJobs;
	}

	public void setMaxJobs(int maxJobs) {
		this.maxJobs = maxJobs;
	}
	

}
