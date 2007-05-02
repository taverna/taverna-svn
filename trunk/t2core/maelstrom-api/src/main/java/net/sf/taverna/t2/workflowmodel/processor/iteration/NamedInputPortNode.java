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

}
