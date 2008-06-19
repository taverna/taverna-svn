package net.sf.taverna.t2.activities.wsdl.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class WSDLActivityItem extends AbstractActivityItem {

	private String use;
	private String url;
	private String style;
	private String operation;

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getType() {
		return "SOAP";
	}

	@Override
	public String toString() {
		return operation;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	
	public Icon getIcon() {
		return new ImageIcon(WSDLActivityItem.class.getResource("/wsdl.png"));
	}
	
	protected Object getConfigBean() {
		WSDLActivityConfigurationBean bean = new WSDLActivityConfigurationBean();
		bean.setWsdl(getUrl());
		bean.setOperation(getOperation());
		return bean;
	}
	
	protected Activity<?> getUnconfiguredActivity() {
		return new WSDLActivity();
	}

}
