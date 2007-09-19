package net.sf.taverna.t2.workflowmodel.processor.activity.impl;

import java.util.List;

import net.sf.taverna.t2.annotation.MimeType;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPortDefinitionBean;

/**
 * <p>
 * A helper that provides an implementation of {@link ActivityPortDefinitionBean}. This may be used as a holder
 * when configuring an Activity that has its ports defined dynamically, or may be used through a delegation model
 * for an Activity that has its ports implicitly defined and uses ConfigType that implements ActivityPortDefinitionBean directly
 * </p>
 * <p>
 * Using this class simplifies configuring the Activity with the use of {@link AbstractActivity#configurePorts(ActivityPortDefinitionBean)}
 * </p>
 * 
 * @author Stuart Owen
 */
public class ActivityPortDefinitionBeanImpl implements ActivityPortDefinitionBean {

	private List<String> inputPortNames;
	private List<Integer> inputPortDepth;
	private List<MimeType> inputPortMimeTypes;
	
	private List<String> outputPortNames;
	
	private List<Integer> outputPortDepth;
	private List<Integer> outputPortGranularDepth;
	private List<MimeType> outputPortMimeTypes;
	
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
	public List<MimeType> getInputPortMimeTypes() {
		return inputPortMimeTypes;
	}
	public void setInputPortMimeTypes(List<MimeType> inputPortMimeType) {
		this.inputPortMimeTypes = inputPortMimeType;
	}
	public List<MimeType> getOutputPortMimeTypes() {
		return outputPortMimeTypes;
	}
	public void setOutputPortMimeTypes(List<MimeType> outputPortMimeType) {
		this.outputPortMimeTypes = outputPortMimeType;
	}	
}
