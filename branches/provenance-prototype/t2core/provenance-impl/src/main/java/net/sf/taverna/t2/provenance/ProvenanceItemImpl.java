package net.sf.taverna.t2.provenance;

import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorType;

public class ProvenanceItemImpl implements ProvenanceItem {

	private List<? extends Activity<?>> activities;
	private Map<String, EntityIdentifier> input;
	private int[] iteration;
	private String owner;

	public ProvenanceItemImpl(List<? extends Activity<?>> activities,
			Map<String, EntityIdentifier> input, int[] iteration, String owner) {
				this.activities = activities;
				this.input = input;
				this.iteration = iteration;
				this.owner = owner;
	}

	public ProvenanceItemImpl() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#getActivity()
	 */
	public List<? extends Activity<?>> getActivity() {
		return activities;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#getError(java.lang.Throwable, java.lang.String, net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorType)
	 */
	public void getError(Throwable cause, String message,
			DispatchErrorType errorType) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#getInput(net.sf.taverna.t2.cloudone.identifier.EntityIdentifier)
	 */
	public void getInput(EntityIdentifier entityIdentifier) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#getOutput(net.sf.taverna.t2.cloudone.identifier.EntityIdentifier)
	 */
	public void getOutput(EntityIdentifier entityIdentifier) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#getOwningProcess(java.lang.String)
	 */
	public void getOwningProcess(String owner) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#setActivities(java.util.List)
	 */
	public void setActivities(List<? extends Activity<?>> activities) {
		this.activities = activities;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#setError(java.lang.Throwable, java.lang.String, net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorType)
	 */
	public void setError(Throwable cause, String message,
			DispatchErrorType errorType) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#setInput(java.util.Map)
	 */
	public void setInput(Map<String, EntityIdentifier> input) {
		this.input = input;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#setIteration(int[])
	 */
	public void setIteration(int[] iteration) {
		this.iteration = iteration;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#setOutput(net.sf.taverna.t2.cloudone.identifier.EntityIdentifier)
	 */
	public void setOutput(Map<String, EntityIdentifier> resultMap) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#setOwningProcess(java.lang.String)
	 */
	public void setOwningProcess(String owner) {
		this.owner = owner;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#getActivities()
	 */
	public List<? extends Activity<?>> getActivities() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#getIteration()
	 */
	public int[] getIteration() {
		return iteration;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.ProvenanceItem#getOwningProcess()
	 */
	public String getOwningProcess() {
		return owner;
	}

	public void setActivity(Activity<?> a) {
		// TODO Auto-generated method stub
		
	}

	public String getAsXML() {
		return owner;
	}

}
