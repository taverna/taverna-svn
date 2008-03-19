package net.sf.taverna.t2.activities.rshell;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;

/**
 * A configuration bean specific to the Rshell activity.
 * 
 */
public class RshellActivityConfigurationBean extends
		ActivityPortsDefinitionBean {

	private String script;

	private RshellConnectionSettings connectionSettings;

	private List<RShellPortSymanticTypeBean> inputSymanticTypes = new ArrayList<RShellPortSymanticTypeBean>();

	private List<RShellPortSymanticTypeBean> outputSymanticTypes = new ArrayList<RShellPortSymanticTypeBean>();

	/**
	 * Returns the script.
	 * 
	 * @return the script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * Sets the script.
	 * 
	 * @param script
	 *            the new script
	 */
	public void setScript(String script) {
		this.script = script;
	}

	/**
	 * Returns the connectionSettings.
	 * 
	 * @return the connectionSettings
	 */
	public RshellConnectionSettings getConnectionSettings() {
		return connectionSettings;
	}

	/**
	 * Sets the connectionSettings.
	 * 
	 * @param connectionSettings
	 *            the new connectionSettings
	 */
	public void setConnectionSettings(
			RshellConnectionSettings connectionSettings) {
		this.connectionSettings = connectionSettings;
	}

	/**
	 * Returns the inputSymanticTypes.
	 *
	 * @return the inputSymanticTypes
	 */
	public List<RShellPortSymanticTypeBean> getInputSymanticTypes() {
		return inputSymanticTypes;
	}

	/**
	 * Sets the inputSymanticTypes.
	 *
	 * @param inputSymanticTypes the new inputSymanticTypes
	 */
	public void setInputSymanticTypes(
			List<RShellPortSymanticTypeBean> inputSymanticTypes) {
		this.inputSymanticTypes = inputSymanticTypes;
	}

	/**
	 * Returns the outputSymanticTypes.
	 *
	 * @return the outputSymanticTypes
	 */
	public List<RShellPortSymanticTypeBean> getOutputSymanticTypes() {
		return outputSymanticTypes;
	}

	/**
	 * Sets the outputSymanticTypes.
	 *
	 * @param outputSymanticTypes the new outputSymanticTypes
	 */
	public void setOutputSymanticTypes(
			List<RShellPortSymanticTypeBean> outputSymanticTypes) {
		this.outputSymanticTypes = outputSymanticTypes;
	}

}
