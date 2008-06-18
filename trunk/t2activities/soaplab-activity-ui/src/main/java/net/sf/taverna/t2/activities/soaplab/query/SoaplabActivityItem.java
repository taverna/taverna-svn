package net.sf.taverna.t2.activities.soaplab.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivityConfigurationBean;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class SoaplabActivityItem extends AbstractActivityItem {
	private String category;
	private String operation;
	private String url;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return "Soaplab";
	}

	@Override
	public String toString() {
		return this.operation;
	}

	public Icon getIcon() {
		return new ImageIcon(SoaplabActivityItem.class
				.getResource("/soaplab.png"));
	}

	@Override
	protected Object getConfigBean() {
		SoaplabActivityConfigurationBean bean = new SoaplabActivityConfigurationBean();
		bean.setEndpoint(getUrl()+getOperation());
		return bean;
	}

	@Override
	protected Activity<?> getUnconfiguredActivity() {
		return new SoaplabActivity();
	}

}
