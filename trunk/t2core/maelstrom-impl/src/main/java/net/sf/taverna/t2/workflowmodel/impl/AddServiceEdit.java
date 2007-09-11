package net.sf.taverna.t2.workflowmodel.impl;

import java.util.List;

import net.sf.taverna.t2.annotation.impl.ServiceAnnotationContainerImpl;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Add a new Service to a Processor, adds the new Service at the end of the
 * current service list for that processor.
 * 
 * @author Tom Oinn
 * 
 */
public class AddServiceEdit extends AbstractProcessorEdit {

	private ServiceAnnotationContainerImpl serviceToAdd;

	public AddServiceEdit(Processor p, Activity<?> s) {
		super(p);
		this.serviceToAdd = new ServiceAnnotationContainerImpl(s);
	}

	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		List<ServiceAnnotationContainerImpl> services = processor.serviceList;
		if (services.contains(serviceToAdd) == false) {
			synchronized (processor) {
				services.add(serviceToAdd);
			}
		} else {
			throw new EditException(
					"Cannot add a duplicate service to processor");
		}

	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		synchronized (processor) {
			processor.serviceList.remove(serviceToAdd);
		}
	}

}
