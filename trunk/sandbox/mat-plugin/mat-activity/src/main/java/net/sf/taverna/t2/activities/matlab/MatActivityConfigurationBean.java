/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.taverna.t2.activities.matlab;

import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;

/**
 * A configuration bean specific to the MatPlugin activity.
 * @author petarj
 */
public class MatActivityConfigurationBean extends ActivityPortsDefinitionBean
{

    private String sctipt;
    private MatPluginConnectionSettings connectionSettings;

    public String getSctipt()
    {
        return sctipt;
    }

    public void setSctipt(String sctipt)
    {
        this.sctipt = sctipt;
    }

    public MatPluginConnectionSettings getConnectionSettings()
    {
        return connectionSettings;
    }

    public void setConnectionSettings(MatPluginConnectionSettings connectionSettings)
    {
        this.connectionSettings = connectionSettings;
    }
}
