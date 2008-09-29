package net.sf.taverna.t2.activities.beanshell.query;

import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class BeanshellActivityItem extends AbstractActivityItem {
	
	private String script;
	
	
	public String getType() {
		return "Beanshell";
	}

	@Override
	public String toString() {
		return getType();
	}

	@Override
	protected Object getConfigBean() {
		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
		bean.setScript("Enter your beanshell script here");
		return bean;
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(BeanshellActivityItem.class.getResource("/beanshell.png"));
	}

	@Override
	protected Activity<?> getUnconfiguredActivity() {
		return new BeanshellActivity();
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}


}
