package net.sf.taverna.t2.workflowmodel.processor.activity.config;


import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * <p>
 * Defines a configuration type that relates directly to an {@link Activity} and in particular defines details its
 * input and output ports.<br>
 * An Activity that has its ports implicitly defined may define a ConfigType that extends this class, but this is not enforced. 
 * </p>
 * 
 * @author Stuart Owen
 *
 */
public class ActivityPortsDefinitionBean {

	private List<ActivityInputPortDefinitionBean> inputs = new ArrayList<ActivityInputPortDefinitionBean>();
	private List<ActivityOutputPortDefinitionBean> outputs = new ArrayList<ActivityOutputPortDefinitionBean>();
	
	
	/**
	 * @return a list of {@link ActivityInputPortDefinitionBean} that describes each input port
	 */
	public List<ActivityInputPortDefinitionBean> getInputPortDefinitions() {
		return inputs;
	}

	/**
	 * @return a list of {@link ActivityOutputPortDefinitionBean} that describes each output port.
	 */
	public List<ActivityOutputPortDefinitionBean> getOutputPortDefinitions() {
		return outputs;
	}

	/**
	 * @param portDefinitions a list of {@link ActivityInputPortDefinitionBean} that describes each input port
	 */
	public void setInputPortDefinitions(
			List<ActivityInputPortDefinitionBean> portDefinitions) {
		inputs=portDefinitions;
	}

	/**
	 * @param portDefinitions a list of {@link ActivityOutputPortDefinitionBean} that describes each output port
	 */
	public void setOutputPortDefinitions(
			List<ActivityOutputPortDefinitionBean> portDefinitions) {
		outputs=portDefinitions;
	}
}
