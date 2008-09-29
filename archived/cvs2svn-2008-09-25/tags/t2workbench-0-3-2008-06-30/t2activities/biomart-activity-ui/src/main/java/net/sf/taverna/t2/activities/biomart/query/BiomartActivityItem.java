package net.sf.taverna.t2.activities.biomart.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.activities.biomart.BiomartActivityConfigurationBean;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartServiceXMLHandler;

public class BiomartActivityItem extends AbstractActivityItem {
	
	private String url;
	private String dataset;
	private String location;
	private MartQuery biomartQuery;

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getType() {
		return "Biomart";
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	@Override
	protected Object getConfigBean() {
		BiomartActivityConfigurationBean bean = new BiomartActivityConfigurationBean();
		bean.setQuery(MartServiceXMLHandler.martQueryToElement(biomartQuery, null));
		return bean;
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(BiomartActivityItem.class.getResource("/biomart.png"));
	}

	@Override
	protected Activity<?> getUnconfiguredActivity() {
		return new BiomartActivity();
	}

	protected void setMartQuery(MartQuery biomartQuery) {
		this.biomartQuery = biomartQuery;
	}

	@Override
	public String toString() {
		return dataset;
	}
	
	

}
