package net.sf.taverna.t2.activities.matlab;

import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;

/**
 * A configuration bean specific to the MatPlugin activity.
 * @author petarj
 */
public class MatActivityConfigurationBean extends ActivityPortsDefinitionBean {

    private String sctipt;
    private MatActivityConnectionSettings connectionSettings;


    public String getSctipt() {
        return sctipt;
    }

    public void setSctipt(String sctipt) {
        this.sctipt = sctipt;
    }

    public MatActivityConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    public void setConnectionSettings(
            MatActivityConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }
}
