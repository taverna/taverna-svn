package net.sf.taverna.t2.workflowmodel.processor.activity.config;

import net.sf.taverna.t2.workflowmodel.OutputPort;

/**
 * A bean that describes properties of an Output port.
 * 
 * @author Stuart Owen
 *
 */
public class ActivityOutputPortDefinitionBean extends ActivityPortDefinitionBean {
	private int granularDepth;

	/**
	 * @return the granular depth of the port
	 * @see OutputPort#getGranularDepth()
	 */
	public int getGranularDepth() {
		return granularDepth;
	}

	/**
	 * @param granularDepth the granular depth of the port
	 */
	public void setGranularDepth(int granularDepth) {
		this.granularDepth = granularDepth;
	}	
}
