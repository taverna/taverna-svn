package net.sf.taverna.t2.cyclone.translators;

import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractServiceConfigurationBean;

/**
 * An configuration bean specific to a Beanshell activity. In particular it provides details
 * about the Beanshell script.
 * 
 * @author Stuart Owen
 */
public class BeanshellActivityConfigurationBean extends AbstractServiceConfigurationBean {

	private String script;

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
	
	
	
}
