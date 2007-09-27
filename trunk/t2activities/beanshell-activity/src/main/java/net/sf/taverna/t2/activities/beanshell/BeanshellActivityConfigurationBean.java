package net.sf.taverna.t2.activities.beanshell;

import net.sf.taverna.t2.workflowmodel.processor.activity.config.impl.ActivityPortsDefinitionBeanImpl;

/**
 * An configuration bean specific to a Beanshell activity. In particular it provides details
 * about the Beanshell script.
 * 
 * @author Stuart Owen
 */
public class BeanshellActivityConfigurationBean extends ActivityPortsDefinitionBeanImpl {

	private String script;

	/**
	 * @return the Beanshell script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * @param script the Beanshell script
	 */
	public void setScript(String script) {
		this.script = script;
	}
}
