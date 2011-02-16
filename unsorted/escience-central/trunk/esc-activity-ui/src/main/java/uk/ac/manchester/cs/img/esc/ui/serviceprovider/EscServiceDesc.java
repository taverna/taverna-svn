package uk.ac.manchester.cs.img.esc.ui.serviceprovider;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import uk.ac.manchester.cs.img.esc.EscActivity;
import uk.ac.manchester.cs.img.esc.EscActivityConfigurationBean;

public class EscServiceDesc extends ServiceDescription<EscActivityConfigurationBean> {

	/**
	 * The subclass of Activity which should be instantiated when adding a service
	 * for this description 
	 */
	@Override
	public Class<? extends Activity<EscActivityConfigurationBean>> getActivityClass() {
		return EscActivity.class;
	}

	/**
	 * The configuration bean which is to be used for configuring the instantiated activity.
	 * Making this bean will typically require some of the fields set on this service
	 * description, like an endpoint URL or method name. 
	 * 
	 */
	@Override
	public EscActivityConfigurationBean getActivityConfiguration() {
		EscActivityConfigurationBean bean = new EscActivityConfigurationBean();
		bean.setName(this.getName());
		bean.setId(this.getId());
		bean.setUrl(this.getUrl());
		return bean;
	}

	/**
	 * An icon to represent this service description in the service palette.
	 */
	@Override
	public Icon getIcon() {
		return EscServiceIcon.getIcon();
	}

	/**
	 * The path to this service description in the service palette. Folders
	 * will be created for each element of the returned path.
	 */
	@Override
	public List<String> getPath() {
		// For deeper paths you may return several strings
		return Arrays.asList("eScience Central workflow: " + url);
	}

	/**
	 * Return a list of data values uniquely identifying this service
	 * description (to avoid duplicates). Include only primary key like fields,
	 * ie. ignore descriptions, icons, etc.
	 */
	@Override
	protected List<? extends Object> getIdentifyingData() {
		// FIXME: Use your fields instead of example fields
		return Arrays.<Object>asList(getId());
	}

	private String name;
	private String id;
	private String url;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}


}
