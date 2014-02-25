/*******************************************************************************
 * Copyright (C) 2009 Ingo Wassink of University of Twente, Netherlands and
 * The University of Manchester
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

/**
 * @author Ingo Wassink
 * @author Ian Dunlop
 * @author Alan R Williams
 */
package net.sf.taverna.t2.activities.rshell;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;

/**
 * A configuration bean specific to the Rshell activity.
 * 
 */
@ConfigurationBean(uri = RshellActivity.URI + "#Config")
public class RshellActivityConfigurationBean extends
        ActivityPortsDefinitionBean {

    private String script;

    private RshellConnectionSettings connectionSettings;

    private List<RShellPortSymanticTypeBean> inputSymanticTypes = new ArrayList<RShellPortSymanticTypeBean>();

    private List<RShellPortSymanticTypeBean> outputSymanticTypes = new ArrayList<RShellPortSymanticTypeBean>();

    /**
     * As XStream is not calling the default constructor during deserialization,
     * we have to set the default values here. This method will be called by
     * XStream after instantiating this bean.
     */
    private Object readResolve() {
        if (connectionSettings != null) {
            if (connectionSettings.getUsername() == null)
                connectionSettings.setUsername("");
            if (connectionSettings.getPassword() == null)
                connectionSettings.setPassword("");
        }
        return this;
    }

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
    @ConfigurationProperty(name = "script", label = "R Script", description = "The R script to be executed")
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
    @ConfigurationProperty(name = "connection", label = "Connection Settings", description = "Settings for connecting to the R server", required = false)
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
     * @param inputSymanticTypes
     *            the new inputSymanticTypes
     */
    @ConfigurationProperty(name = "inputSemanticTypes", label = "Semantic Input Ports", description = "Inputs to the R script", required = false)
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
     * @param outputSymanticTypes
     *            the new outputSymanticTypes
     */
    @ConfigurationProperty(name = "outputSemanticTypes", label = "Semantic Output Ports", description = "Outputs from the R script", required = false)
    public void setOutputSymanticTypes(
            List<RShellPortSymanticTypeBean> outputSymanticTypes) {
        this.outputSymanticTypes = outputSymanticTypes;
    }

}
