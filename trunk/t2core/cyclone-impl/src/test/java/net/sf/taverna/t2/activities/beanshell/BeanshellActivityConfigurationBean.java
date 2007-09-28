package net.sf.taverna.t2.activities.beanshell;

import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;

/**
 * An dummy configuration bean specific to a Beanshell activity for testing
 * 
 * @author Stuart Owen
 */
public class BeanshellActivityConfigurationBean extends ActivityPortsDefinitionBean {

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
