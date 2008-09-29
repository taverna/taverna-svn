package net.sf.taverna.t2.activities.stringconstant.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class StringConstantActivityItem extends AbstractActivityItem {

	public String getType() {
		return "String Constant";
	}

	@Override
	public String toString() {
		return getType();
	}

	public Icon getIcon() {
		return new ImageIcon(StringConstantActivityItem.class
				.getResource("/stringconstant.png"));
	}

	
	@Override
	protected Object getConfigBean() {
		StringConstantConfigurationBean configbean = new StringConstantConfigurationBean();
		configbean.setValue("Add your own value here");
		return configbean;
		
	}

	@Override
	protected Activity<?> getUnconfiguredActivity() {
		return new StringConstantActivity();
	}

}
