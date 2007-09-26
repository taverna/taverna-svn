package net.sf.taverna.t2.workflowmodel.processor.activity.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;

/**
 * <p>
 * A helper that provides an implementation of {@link ActivityPortsDefinitionBean}. This may be used as a intermediate 
 * when configuring an Activity that has its ports defined dynamically, or may be used directly as the implementation of ActivityPortDefinitionBean 
 * for the ConfigType for an Activity that defines its ports explicitly.
 * </p>
 * <p>
 * Using this class simplifies configuring the Activity with the use of {@link AbstractActivity#configurePorts(ActivityPortsDefinitionBean)}
 * </p>
 * 
 * @author Stuart Owen
 */
public class ActivityPortsDefinitionBeanImpl implements ActivityPortsDefinitionBean {

	private List<ActivityInputPortDefinitionBean> inputs = new ArrayList<ActivityInputPortDefinitionBean>();
	private List<ActivityOutputPortDefinitionBean> outputs = new ArrayList<ActivityOutputPortDefinitionBean>();
	
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean#getInputPortDefinitions()
	 */
	public List<ActivityInputPortDefinitionBean> getInputPortDefinitions() {
		return inputs;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean#getOutputPortDefinitions()
	 */
	public List<ActivityOutputPortDefinitionBean> getOutputPortDefinitions() {
		return outputs;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean#setInputPortDefinitions(java.util.List)
	 */
	public void setInputPortDefinitions(
			List<ActivityInputPortDefinitionBean> portDefinitions) {
		inputs=portDefinitions;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean#setOutputPortDefinitions(java.util.List)
	 */
	public void setOutputPortDefinitions(
			List<ActivityOutputPortDefinitionBean> portDefinitions) {
		outputs=portDefinitions;
	}
}
