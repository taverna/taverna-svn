package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;

/**
 * An implementation of the filtering input port interface used as an input for
 * a ProcessorImpl. If the filter level is undefined this input port will always
 * throw workflow structure exceptions when you push data into it. This port
 * must be linked to a crystalizer or something which offers the same
 * operational contract, it requires a full hierarchy of data tokens (i.e. if
 * you push something in with an index you must at some point subsequent to that
 * push at least a single list in with the empty index)
 * 
 * @author Tom Oinn
 * 
 */
public class ProcessorInputPortImpl extends AbstractFilteringInputPort implements
		ProcessorInputPort {

	private ProcessorImpl parent;

	protected ProcessorInputPortImpl(ProcessorImpl parent, String name,
			int depth) {
		super(name, depth);
		this.parent = parent;
	}

	public String transformOwningProcess(String oldOwner) {
		return oldOwner + ":" + parent.getLocalName();
	}
	
	@Override
	protected void pushCompletion(String portName, String owningProcess, int[] index, InvocationContext context) {
		parent.iterationStack.receiveCompletion(portName, owningProcess, index, context);	
	}

	@Override
	protected void pushData(String portName, String owningProcess, int[] index, EntityIdentifier data, InvocationContext context) {
		parent.iterationStack.receiveData(portName, owningProcess, index, data, context);
	}
	
	public Processor getProcessor() {
		return this.parent;
	}
	
}
