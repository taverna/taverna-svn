package net.sf.taverna.t2.workflowmodel;

/**
 * Input port on a Processor, is both an event forwarding output port and a
 * processor port
 * 
 * @author Tom Oinn
 * 
 */
public interface ProcessorOutputPort extends EventForwardingOutputPort,
		ProcessorPort {

}
