package net.sf.taverna.t2.component.ui.serviceprovider;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.wraplog.Logger;

public class ComponentServiceDesc extends ServiceDescription<ComponentActivityConfigurationBean> {
	
	private static Logger logger = Logger.getLogger(ComponentServiceDesc.class);
	
	private String name;
	private String familyName;

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	/**
	 * The subclass of Activity which should be instantiated when adding a service
	 * for this description 
	 */
	@Override
	public Class<? extends Activity<ComponentActivityConfigurationBean>> getActivityClass() {
		return ComponentActivity.class;
	}

	/**
	 * The configuration bean which is to be used for configuring the instantiated activity.
	 * Making this bean will typically require some of the fields set on this service
	 * description, like an endpoint URL or method name. 
	 * 
	 */
	@Override
	public ComponentActivityConfigurationBean getActivityConfiguration() {
		ComponentActivityConfigurationBean bean = new ComponentActivityConfigurationBean();
		bean.setDataflowString(dataflowString);
		return bean;
	}

	/**
	 * An icon to represent this service description in the service palette.
	 */
	@Override
	public Icon getIcon() {
		return ComponentServiceIcon.getIcon();
	}

	/**
	 * The display name that will be shown in service palette and will
	 * be used as a template for processor name when added to workflow.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * The path to this service description in the service palette. Folders
	 * will be created for each element of the returned path.
	 */
	@Override
	public List<String> getPath() {
		// For deeper paths you may return several strings
		return Arrays.asList("Component family " + familyName);
	}

	/**
	 * Return a list of data values uniquely identifying this service
	 * description (to avoid duplicates). Include only primary key like fields,
	 * ie. ignore descriptions, icons, etc.
	 */
	@Override
	protected List<? extends Object> getIdentifyingData() {
		// FIXME: Use your fields instead of example fields
		return Arrays.<Object>asList(familyName, name);
	}

	private String dataflowString;

	public String getDataflowString() {
		return dataflowString;
	}

	public void setDataflowString(String dataflowString) {
		this.dataflowString = dataflowString;
		try {
			Dataflow d = ComponentActivity.openDataflowString(dataflowString);
		} catch (ActivityConfigurationException e) {
			logger.error("Unable to parse component dataflow", e);
		}
	}

	public void setName(String name) {
		this.name = name;
	}

}
