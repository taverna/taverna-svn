package net.sf.taverna.t2.workflowmodel.impl;

import java.util.List;

import net.sf.taverna.t2.annotation.impl.AbstractMutableAnnotatedThing;
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
		List<Service<?>> services = processor.serviceList;
		if (services.contains(serviceToAdd) == false) {
			synchronized (processor) {
				services.add(serviceToAdd);
				processor.serviceAnnotations
						.add(new AbstractMutableAnnotatedThing());
			}
		} else {
			throw new EditException(
					"Cannot add a duplicate service to processor");
		}

	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		synchronized (processor) {
			processor.serviceAnnotations.remove(processor.serviceList
					.indexOf(serviceToAdd));
			processor.serviceList.remove(serviceToAdd);
		}
	}

}
