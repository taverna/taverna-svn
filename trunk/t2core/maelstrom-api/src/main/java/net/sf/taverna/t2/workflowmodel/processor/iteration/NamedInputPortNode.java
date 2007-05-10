package net.sf.taverna.t2.workflowmodel.processor.iteration;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;

/**
 * Acts as the input to a stage within the iteration strategy, passes all jobs
 * straight through. NamedInputPortNode objects are, as the name suggests,
 * named. These names correspond to the names of abstract input ports on the
 * Processor object to which the iteration strategy belongs.
 * 
 * @author Tom Oinn
 * 
 */
public class NamedInputPortNode extends AbstractIterationStrategyNode {

	private String portName;

	private int desiredCardinality;

	// FIXME - this should really be per-process or at least be possible to
	// reset.
	private int oid = -1;

	public NamedInputPortNode(String name, int cardinality) {
		super();
		this.portName = name;
		this.desiredCardinality = cardinality;
	}

	/**
	 * If this node receives a job it will always be pushed without modification
	 * up to the parent
	 */
	public void receiveJob(int inputIndex, Job newJob) {
		pushJob(newJob);
	}

	/**
	 * Completion events are passed straight through the same as jobs
	 */
	public void receiveCompletion(int inputIndex, Completion completion) {
		pushCompletion(completion);
	}

	/**
	 * Each node maps to a single named input port within the processor
	 */
	public String getPortName() {
		return this.portName;
	}

	/**
	 * Each node defines the level of collection depth for that input port
	 */
	public int getCardinality() {
		return this.desiredCardinality;
	}

	/**
	 * These nodes correspond to inputs to the iteration strategy and are always
	 * leaf nodes as a result.
	 * 
	 * @override
	 */
	public boolean isLeaf() {
		return true;
	}

	/**
	 * These nodes can never have children
	 * 
	 * @override
	 */
	public boolean getAllowsChildren() {
		return false;
	}

	/**
	 * The iteration depth is the difference between cardinality of the given
	 * and expected input data plus the index array length of that data. For
	 * example, if this input port expected a single item and was given an array
	 * with an index length 2 the value returned here would be 3, one for the
	 * single step mismatch in depth and two for the index array defining the
	 * position of that input list within its parent collection. This is set by
	 * the internal logic of the workflow when data is first received by a
	 * processor input port.
	 */
	public void setObservedIterationDepth(int oid) {
		this.oid = oid;
	}

	/**
	 * Returns the value defined by the setObservedIterationDepth method.
	 */
	public int getIterationDepth() {
		return this.oid;
	}

}
