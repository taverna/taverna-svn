package net.sf.taverna.t2.workflowmodel;

import java.util.List;
import java.util.Map;

/**
 * Top level definition object for a dataflow workflow. Currently Taverna only
 * supports dataflow workflows, this is equivalent to the Taverna 1 ScuflModel
 * class in role.
 * 
 * @author Tom Oinn
 * 
 */
public interface Dataflow {

	public Map<String, Processor> getNamedProcessors();
	
	public List<InputPort> getInputPorts();
	
	public List<OutputPort> getOutputPorts();
	
}
