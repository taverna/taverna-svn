package net.sf.taverna.t2.workflowmodel.impl;

import java.util.Collections;
import java.util.List;
import net.sf.taverna.t2.annotation.impl.AbstractMutableAnnotatedThing;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;

public class DataflowImpl extends AbstractMutableAnnotatedThing implements
		Dataflow {

	private List<ProcessorImpl> processors;
	
	public List<DataflowInputPort> getInputPorts() {
		return null;
	}

	public List<Datalink> getLinks() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<? extends Processor> getProcessors() {
		return Collections.unmodifiableList(this.processors);
	}

	public List<DataflowOutputPort> getOutputPorts() {
		// TODO Auto-generated method stub
		return null;
	}

}
