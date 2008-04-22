package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeOutputPort;

public class MergeOutputPortImpl extends BasicEventForwardingOutputPort implements MergeOutputPort {
	
	private Merge merge;
	
	public MergeOutputPortImpl(Merge merge,String portName, int portDepth, int granularDepth) {
		super(portName, portDepth, granularDepth);
		this.merge=merge;
	}
	
	public Merge getMerge() {
		return merge;
	}
	
}
