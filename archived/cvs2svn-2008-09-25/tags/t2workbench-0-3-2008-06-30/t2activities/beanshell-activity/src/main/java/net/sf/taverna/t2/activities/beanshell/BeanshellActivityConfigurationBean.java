package net.sf.taverna.t2.activities.beanshell;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;

/**
 * An configuration bean specific to a Beanshell activity. In particular it provides details
 * about the Beanshell script.
 * 
 * @author Stuart Owen
 * @author David Withers
 */
public class BeanshellActivityConfigurationBean extends ActivityPortsDefinitionBean {

	private String script;
	
	private List<String> dependencies = new ArrayList<String>();

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

	/**
	 * Returns the dependencies.
	 *
	 * @return the dependencies
	 */
	public List<String> getDependencies() {
		return dependencies;
	}

	/**
	 * Sets the dependencies.
	 *
	 * @param dependencies the new dependencies
	 */
	public void setDependencies(List<String> dependencies) {
		this.dependencies = dependencies;
	}
	
}
