package net.sf.taverna.t2.activities.rshell.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.rshell.RshellActivity;
import net.sf.taverna.t2.activities.rshell.RshellActivityConfigurationBean;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class RshellActivityItem extends AbstractActivityItem{

	@Override
	protected Object getConfigBean() {
		RshellActivityConfigurationBean bean = new RshellActivityConfigurationBean();
		return bean;
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(RshellActivityItem.class.getResource("/rshell.png"));
	}

	@Override
	protected Activity<?> getUnconfiguredActivity() {
		return new RshellActivity();
	}
	
	public String getType() {
		return "Rshell";
	}
	
	@Override
	public String toString() {
		return getType();
	}

}
