package net.sf.taverna.t2.matlabactivity.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import net.sf.taverna.t2.activities.matlab.MatActivity;
import net.sf.taverna.t2.activities.matlab.MatActivityConfigurationBean;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 *
 * @author user
 */
public class MatActivityItem extends AbstractActivityItem {

    @Override
    public Icon getIcon() {
        return new ImageIcon(MatActivityItem.class.getResource("/maticon.PNG"));
    }

    public String getType() {
        return "MatPlugin";
    }

    @Override
    protected Object getConfigBean() {
        MatActivityConfigurationBean bean = new MatActivityConfigurationBean();
        return bean;
    }

    @Override
    protected Activity<?> getUnconfiguredActivity() {
        return new MatActivity();
    }
}
