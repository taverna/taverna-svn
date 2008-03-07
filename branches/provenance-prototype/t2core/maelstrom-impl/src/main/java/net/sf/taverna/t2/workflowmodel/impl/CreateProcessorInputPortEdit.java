package net.sf.taverna.t2.workflowmodel.impl;

import org.jdom.Element;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;

/**
 * Build a new input port on a processor, also modifies the processor's
 * iteration strategy or strategies to ensure the new port is bound into them.
 * 
 * @author Tom Oinn
 * 
 */
public class CreateProcessorInputPortEdit extends AbstractProcessorEdit {

	String newPortName;

	Element previousIterationStrategyState = null;

	int newPortDepth;

	public CreateProcessorInputPortEdit(Processor p, String portName,
			int portDepth) {
		super(p);
		this.newPortName = portName;
		this.newPortDepth = portDepth;
	}

	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		// Add a new InputPort object to the processor and also create an
		// appropriate NamedInputPortNode in any iteration strategies. By
		// default set the desired drill depth on each iteration strategy node
		// to the same as the input port, so this won't automatically trigger
		// iteration staging unless the depth is altered on the iteration
		// strategy itself.)
		if (processor.getInputPortWithName(newPortName) != null) {
			throw new EditException(
					"Attempt to create duplicate input port with name '"
							+ newPortName + "'");
		}
		previousIterationStrategyState = processor.iterationStack.asXML();
		processor.inputPorts.add(new ProcessorInputPortImpl(processor, newPortName,
				newPortDepth));
		for (IterationStrategyImpl is : processor.iterationStack.getStrategies()) {
			NamedInputPortNode nipn = new NamedInputPortNode(newPortName,
					newPortDepth);
			is.addInput(nipn);
			is.connectDefault(nipn);
		}

	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		processor.iterationStack
				.configureFromElement(previousIterationStrategyState);
		processor.inputPorts
				.remove(processor.getInputPortWithName(newPortName));
	}

}
