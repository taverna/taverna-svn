package net.sf.taverna.t2.workflowmodel.processor.activity.config;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * <p>
 * Defines a configuration type that relates directly to an {@link Activity} and in particular defines details its
 * input and output ports.<br>
 * An Activity that has its ports implicitly defined may define a ConfigType that implements this interface, but this is not enforced. 
 * </p>
 * 
 * @author Stuart Owen
 *
 */
public interface ActivityPortsDefinitionBean {

	/**
	 * @return a list of {@link ActivityInputPortDefinitionBean} that describes each input port
	 */
	List<ActivityInputPortDefinitionBean>getInputPortDefinitions();
	/**
	 * @param portDefinitions a list of {@link ActivityInputPortDefinitionBean} that describes each input port
	 */
	void setInputPortDefinitions(List<ActivityInputPortDefinitionBean> portDefinitions);
	
	/**
	 * @return a list of {@link ActivityOutputPortDefinitionBean} that describes each output port.
	 */
	List<ActivityOutputPortDefinitionBean>getOutputPortDefinitions();
	/**
	 * @param portDefinitions a list of {@link ActivityOutputPortDefinitionBean} that describes each output port
	 */
	void setOutputPortDefinitions(List<ActivityOutputPortDefinitionBean> portDefinitions);
}
