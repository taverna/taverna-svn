package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.apache.log4j.Logger;

/**
 * An Edit that is responsible for configuring an Activity with a given configuration bean.
 * 
 * @author Stuart Owen
 */
public class ConfigureActivityEdit implements Edit<Activity<?>> {
	
	private static Logger logger = Logger
			.getLogger(ConfigureActivityEdit.class);
	
	private final Activity<?> activity;
	private final Object configurationBean;
	private boolean isApplied=false;

	public ConfigureActivityEdit(Activity<?>activity, Object configurationBean ) {
		this.activity = activity;
		this.configurationBean = configurationBean;
		
	}
	@SuppressWarnings("unchecked")
	public Activity<?> doEdit() throws EditException {
		try {
			((Activity)activity).configure(configurationBean);
		} catch (ActivityConfigurationException e) {
			logger.error("Error configuring the activity:"+activity.getClass().getSimpleName(),e);
			throw new EditException(e);
		}
		isApplied=true;
		return activity;
	}

	public Object getSubject() {
		return activity;
	}

	public boolean isApplied() {
		return isApplied;
	}

	public void undo() {
		// TODO Auto-generated method stub
	}

}
