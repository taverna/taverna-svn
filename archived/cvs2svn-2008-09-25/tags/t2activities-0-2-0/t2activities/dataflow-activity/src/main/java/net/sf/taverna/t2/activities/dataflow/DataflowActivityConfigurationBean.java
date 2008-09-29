package net.sf.taverna.t2.activities.dataflow;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * A configuration bean specific to a Dataflow activity.
 * 
 * @author David Withers
 */
public class DataflowActivityConfigurationBean {

	private Dataflow dataflow;

	/**
	 * Returns the dataflow.
	 * 
	 * @return the dataflow
	 */
	public Dataflow getDataflow() {
		return dataflow;
	}

	/**
	 * Sets the dataflow.
	 * 
	 * @param dataflow
	 *            the new dataflow
	 */
	public void setDataflow(Dataflow dataflow) {
		this.dataflow = dataflow;
	}

}
