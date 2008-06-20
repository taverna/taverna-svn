package net.sf.taverna.t2.activities.localworker.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class LocalworkerActivityItem extends AbstractActivityItem {

	public String getType() {
		return "Localworker";
	}

	@Override
	protected Object getConfigBean() {
		// TODO Auto-generated method stub
		// different bean for each type of localworker, get xml version of bean
		// and create BeanshellConfig
		return null;
	}

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return new ImageIcon(LocalworkerActivityItem.class.getResource("/localworker.png"));
	}

	@Override
	protected Activity<?> getUnconfiguredActivity() {
		// TODO Auto-generated method stub
		return new BeanshellActivity();
	}
}
