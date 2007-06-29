package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.DataLink;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;

public class MergeInputPortImpl extends AbstractPort implements MergeInputPort {

	private MergeImpl parent;
	
	protected MergeInputPortImpl(MergeImpl merge, String name, int depth) {
		super(name, depth);
		this.parent = merge;
	}

	public void receiveEvent(Event e) {
		parent.receiveEvent(e, this.name);
	}

	public DataLink getIncomingLink() {
		// TODO Auto-generated method stub
		return null;
	}


}
