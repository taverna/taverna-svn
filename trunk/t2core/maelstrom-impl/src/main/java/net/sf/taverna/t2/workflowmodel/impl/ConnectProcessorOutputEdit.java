package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.DataLink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.FilteringInputPort;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * Connect the named output port on a given processor to the specified
 * EventHandlingInputPort, updating the connected port list on the output port
 * of the processor such that events will be forwarded as appropriate. If the
 * target port is a FilteringInputPort then also set the filter level
 * appropriately.
 * 
 * @author Tom Oinn
 * 
 */
public class ConnectProcessorOutputEdit extends AbstractProcessorEdit {

	private EventHandlingInputPort target;

	private String outputName;

	private ProcessorOutputPortImpl outputPort;
	
	private DataLink newLink = null;

	public ConnectProcessorOutputEdit(Processor p, String outputName,
			EventHandlingInputPort targetPort) {
		super(p);
		this.target = targetPort;
		this.outputName = outputName;
	}

	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		for (ProcessorOutputPortImpl popi : processor.outputPorts) {
			if (popi.getName().equals(outputName)) {
				newLink = new DataLinkImpl(popi, target);
				popi.addOutgoingLink(newLink);
				if (target instanceof AbstractEventHandlingInputPort) {
					((AbstractFilteringInputPort)target).setIncomingLink(newLink);
				}
				//popi.addTarget(target);
				if (target instanceof FilteringInputPort) {
					FilteringInputPort fip = (FilteringInputPort) target;
					// Set the filter to filter on the granular depth of this
					// output port unless this is finer than the input port
					// depth in which case set the filter to be equal to that
					// (i.e. if the input port depth is a list we never set the
					// filter to be a single item, it's always the list level.
					// If the granular depth were a list of lists then we'd use
					// that).
					int gDepth = popi.getGranularDepth();
					int pDepth = fip.getDepth();
					if (gDepth > pDepth) {
						fip.setFilterDepth(gDepth);
					} else {
						fip.setFilterDepth(pDepth);
					}
				}
				outputPort = popi;
				return;
			}
		}
		throw new EditException("Cannot locate output port with name '"
				+ outputName + "'");
	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		outputPort.removeOutgoingLink(newLink);
		if (target instanceof AbstractEventHandlingInputPort) {
			((AbstractEventHandlingInputPort)target).setIncomingLink(null);
		}
	}

}
