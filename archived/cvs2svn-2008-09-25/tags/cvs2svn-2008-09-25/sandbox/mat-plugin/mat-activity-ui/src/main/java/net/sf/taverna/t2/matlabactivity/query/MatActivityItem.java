package net.sf.taverna.t2.matlabactivity.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import net.sf.taverna.t2.activities.matlab.MatActivity;
import net.sf.taverna.t2.activities.matlab.MatActivityConfigurationBean;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 *
 * @author petarj
 */
public class MatActivityItem extends AbstractActivityItem {

    private String script;

    public String getType() {
        return "MatPlugin";
    }

    @Override
    public String toString() {
        return getType();
    }

    @Override
    protected Object getConfigBean() {
        MatActivityConfigurationBean bean = new MatActivityConfigurationBean();
        bean.setSctipt("%Enter your matlab script here");
        return bean;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(MatActivityItem.class.getResource("/maticon.PNG"));
    }

    @Override
    protected Activity<?> getUnconfiguredActivity() {
        return new MatActivity();
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
    
    
}
