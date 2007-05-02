package net.sf.taverna.t2.workflowmodel.impl;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.service.Service;

/**
 * Add a new Service to a Processor, adds the new Service at the end of the
 * current service list for that processor.
 * 
 * @author Tom Oinn
 * 
 */
public class AddServiceEdit extends AbstractProcessorEdit {

	private Service<?> serviceToAdd;

	public AddServiceEdit(Processor p, Service<?> s) {
		super(p);
		this.serviceToAdd = s;
	}

	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		List<Service<?>> services = processor.getServiceList();
		if (services.contains(serviceToAdd) == false) {
			services.add(serviceToAdd);
		} else {
			throw new EditException(
					"Cannot add a duplicate service to processor");
		}

	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		processor.getServiceList().remove(serviceToAdd);
	}

}
