package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.util.List;

/**
 * <p>
 * Describes the fundamental requirements to configure a service.
 * These are the input and output port definitions.
 * </p>
 * <p>
 * It is not essential that an Activity ConfigType should extend this abstract implementation,
 * but doing so simplifies configuring the Activity with the use of {@link AbstractActivity#configurePorts(ActivityConfigurationBean)}
 * </p>
 * 
 * @author Stuart Owen
 */
public abstract class AbstractServiceConfigurationBean implements ActivityConfigurationBean {

	private List<String> inputPortNames;
	private List<String> outputPortNames;
	
	private List<Integer> inputPortDepth;
	private List<Integer> outputPortDepth;
	private List<Integer> outputPortGranularDepth;
	
	public List<String> getInputPortNames() {
		return inputPortNames;
	}
	public void setInputPortNames(List<String> inputPortNames) {
		this.inputPortNames = inputPortNames;
	}
	public List<String> getOutputPortNames() {
		return outputPortNames;
	}
	public void setOutputPortNames(List<String> outputPortNames) {
		this.outputPortNames = outputPortNames;
	}
	public List<Integer> getInputPortDepth() {
		return inputPortDepth;
	}
	public void setInputPortDepth(List<Integer> inputDepth) {
		this.inputPortDepth = inputDepth;
	}
	public List<Integer> getOutputPortDepth() {
		return outputPortDepth;
	}
	public void setOutputPortDepth(List<Integer> outputPortDepth) {
		this.outputPortDepth = outputPortDepth;
	}
	public List<Integer> getOutputPortGranularDepth() {
		return outputPortGranularDepth;
	}
	public void setOutputPortGranularDepth(List<Integer> outputPortGranularDepth) {
		this.outputPortGranularDepth = outputPortGranularDepth;
	}
	
	
	
}
