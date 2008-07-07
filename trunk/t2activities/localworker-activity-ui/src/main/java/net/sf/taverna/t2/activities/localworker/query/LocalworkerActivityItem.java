package net.sf.taverna.t2.activities.localworker.query;

import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.localworker.LocalworkerActivity;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

/**
 * Represents a Localworker activity on the Activity Tree/Partition which can be
 * dragged. It creates a Local Worker Activity through a
 * {@link BeanshellActivityConfigurationBean} populated with the script and
 * ports
 * 
 * @author Ian Dunlop
 * 
 */
public class LocalworkerActivityItem extends AbstractActivityItem {

	private String script;
	private List<ActivityOutputPortDefinitionBean> outputPorts;
	private List<ActivityInputPortDefinitionBean> inputPorts;
	private String operation;
	private String category;
	private String provider;

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getType() {
		return "Localworker";
	}

	/**
	 * Returns a {@link BeanshellActivityConfigurationBean} which represents
	 * this local worker
	 */
	@Override
	protected Object getConfigBean() {
		// TODO Auto-generated method stub
		// different bean for each type of localworker, get xml version of bean
		// and create BeanshellConfig
		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
		bean.setScript(this.script);
		bean.setInputPortDefinitions(this.inputPorts);
		bean.setOutputPortDefinitions(this.outputPorts);

		// FIXME needs some mime types from the annotations (done as strings
		// inside the port at the moment)
		return bean;
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(LocalworkerActivityItem.class
				.getResource("/localworker.png"));
	}

	/**
	 * Returns a {@link BeanshellActivity} which represents this local worker
	 */
	@Override
	protected Activity<?> getUnconfiguredActivity() {
		Activity<?> activity = new LocalworkerActivity();
		return activity;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public void setOutputPorts(
			List<ActivityOutputPortDefinitionBean> outputPortBeans) {
		this.outputPorts = outputPortBeans;
	}

	public List<ActivityOutputPortDefinitionBean> getOutputPorts() {
		return outputPorts;
	}

	public void setInputPorts(
			List<ActivityInputPortDefinitionBean> inputPortBeans) {
		this.inputPorts = inputPortBeans;
	}

	public List<ActivityInputPortDefinitionBean> getInputPorts() {
		return inputPorts;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	@Override
	public String toString() {
		return operation;
	}
}
