package net.sf.taverna.t2.activities.biomoby.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.biomoby.BiomobyActivity;
import net.sf.taverna.t2.activities.biomoby.BiomobyActivityConfigurationBean;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class BiomobyActivityItem extends AbstractActivityItem {

	String registryUrl;
	String serviceName;
	String authorityName;
	String registryUri;
	
	public String getRegistryUri() {
		return registryUri;
	}

	public void setRegistryUri(String registryUri) {
		this.registryUri = registryUri;
	}

	public String getRegistryUrl() {
		return registryUrl;
	}

	public void setRegistryUrl(String registryUrl) {
		this.registryUrl = registryUrl;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getAuthorityName() {
		return authorityName;
	}

	public void setAuthorityName(String authorityName) {
		this.authorityName = authorityName;
	}

	@Override
	protected Object getConfigBean() {
		BiomobyActivityConfigurationBean bean = new BiomobyActivityConfigurationBean();
		bean.setAuthorityName(getAuthorityName());
		bean.setServiceName(getServiceName());
		bean.setMobyEndpoint(getRegistryUrl());
		return bean;
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(BiomobyActivityItem.class.getResource("/registry.gif"));
	}

	@Override
	protected Activity<?> getUnconfiguredActivity() {
		return new BiomobyActivity();
	}
	
	public String getType() {
		return "Biomoby";
	}

	@Override
	public String toString() {
		return getServiceName();
	}
	
}
