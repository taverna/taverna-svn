package net.sf.taverna.t2.workflowmodel.impl;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Add a new Activity to a Processor, adding the new Activity at the end of the
 * current activity list for that processor.
 * 
 * @author Tom Oinn
 * 
 */
public class AddActivityEdit extends AbstractProcessorEdit {

	private Activity<?> activityToAdd;

	public AddActivityEdit(Processor processor, Activity<?> activity) {
		super(processor);
		this.activityToAdd = activity;
	}

	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		List<Activity<?>> activities = processor.activityList;
		if (activities.contains(activityToAdd) == false) {
			synchronized (processor) {
				activities.add(activityToAdd);
			}
		} else {
			throw new EditException(
					"Cannot add a duplicate activity to processor");
		}

	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		synchronized (processor) {
			processor.activityList.remove(activityToAdd);
		}
	}

}
