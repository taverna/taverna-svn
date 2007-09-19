package net.sf.taverna.t2.cyclone.translators;

import java.util.List;

import net.sf.taverna.t2.annotation.MimeType;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityPortDefinitionBeanImpl;

/**
 * An configuration bean specific to a Beanshell activity. In particular it provides details
 * about the Beanshell script.
 * 
 * @author Stuart Owen
 */
public class BeanshellActivityConfigurationBean implements ActivityPortDefinitionBean {

	private String script;
	private ActivityPortDefinitionBean portDefinitionBean = new ActivityPortDefinitionBeanImpl();

	/**
	 * @return the Beanshell script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * @param the Beanshell script
	 */
	public void setScript(String script) {
		this.script = script;
	}

	public List<Integer> getInputPortDepth() {
		return portDefinitionBean.getInputPortDepth();
	}

	public List<MimeType> getInputPortMimeTypes() {
		return portDefinitionBean.getInputPortMimeTypes();
	}

	public List<String> getInputPortNames() {
		return portDefinitionBean.getInputPortNames();
	}

	public List<Integer> getOutputPortDepth() {
		return portDefinitionBean.getOutputPortDepth();
	}

	public List<Integer> getOutputPortGranularDepth() {
		return portDefinitionBean.getOutputPortGranularDepth();
	}

	public List<MimeType> getOutputPortMimeTypes() {
		return portDefinitionBean.getOutputPortMimeTypes();
	}

	public List<String> getOutputPortNames() {
		return portDefinitionBean.getOutputPortNames();
	}

	public void setInputPortDepth(List<Integer> inputDepth) {
		portDefinitionBean.setInputPortDepth(inputDepth);
	}

	public void setInputPortMimeTypes(List<MimeType> mimeType) {
		portDefinitionBean.setInputPortMimeTypes(mimeType);
	}

	public void setInputPortNames(List<String> inputNames) {
		portDefinitionBean.setInputPortNames(inputNames);
	}

	public void setOutputPortDepth(List<Integer> outputDepth) {
		portDefinitionBean.setOutputPortDepth(outputDepth);
	}

	public void setOutputPortGranularDepth(List<Integer> outputGranularDepth) {
		portDefinitionBean.setOutputPortGranularDepth(outputGranularDepth);
	}

	public void setOutputPortMimeTypes(List<MimeType> mimeTypes) {
		portDefinitionBean.setOutputPortMimeTypes(mimeTypes);
	}

	public void setOutputPortNames(List<String> outputNames) {
		portDefinitionBean.setOutputPortNames(outputNames);
	}	
}
