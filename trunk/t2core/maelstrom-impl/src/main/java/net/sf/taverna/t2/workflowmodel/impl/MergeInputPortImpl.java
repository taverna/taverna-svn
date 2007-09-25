package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;

public class MergeInputPortImpl extends AbstractEventHandlingInputPort implements MergeInputPort {

	private MergeImpl parent;
	
	protected MergeInputPortImpl(MergeImpl merge, String name, int depth) {
		super(name, depth);
		this.parent = merge;
	}

	public void receiveEvent(Event e) {
		parent.receiveEvent(e, this.name);
	}

	public Merge getMergeInstance() {
		return parent;
	}

}
