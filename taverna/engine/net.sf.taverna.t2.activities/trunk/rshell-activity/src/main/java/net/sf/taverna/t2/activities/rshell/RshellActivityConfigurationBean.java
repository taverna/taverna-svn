/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
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
	
	/** false for old r versions, true for newer (1.6+) */
	private boolean rVersion;

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
		if (script == null) {
			setScript("");
		}
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
		if (connectionSettings == null) {
			setConnectionSettings(RshellConnectionSettings.defaultSettings());
		}
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
