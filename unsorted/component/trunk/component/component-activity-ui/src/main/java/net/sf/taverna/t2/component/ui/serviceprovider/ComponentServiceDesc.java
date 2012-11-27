package net.sf.taverna.t2.component.ui.serviceprovider;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.log4j.Logger;

public class ComponentServiceDesc extends ServiceDescription<ComponentActivityConfigurationBean> {
	
	private static Logger logger = Logger.getLogger(ComponentServiceDesc.class);
	
	private URL registryBase;
	
	private String familyName;
	
	private String componentName;
	
	private Integer componentVersion;

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
		ComponentActivityConfigurationBean bean = new ComponentActivityConfigurationBean(registryBase, familyName, componentName, componentVersion);
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
		return componentName;
	}

	/**
	 * The path to this service description in the service palette. Folders
	 * will be created for each element of the returned path.
	 */
	@Override
	public List<String> getPath() {
		// For deeper paths you may return several strings
		return Arrays.asList(registryBase.toString(), familyName);
	}

	/**
	 * Return a list of data values uniquely identifying this service
	 * description (to avoid duplicates). Include only primary key like fields,
	 * ie. ignore descriptions, icons, etc.
	 */
	@Override
	protected List<? extends Object> getIdentifyingData() {
		// FIXME: Use your fields instead of example fields
		return Arrays.<Object>asList(registryBase, familyName, componentName);
	}

	/**
	 * @return the registryBase
	 */
	public URL getRegistryBase() {
		return registryBase;
	}

	/**
	 * @param registryBase the registryBase to set
	 */
	public void setRegistryBase(URL registryBase) {
		this.registryBase = registryBase;
	}

	/**
	 * @return the familyName
	 */
	public String getFamilyName() {
		return familyName;
	}

	/**
	 * @param familyName the familyName to set
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	/**
	 * @return the componentName
	 */
	public String getComponentName() {
		return componentName;
	}

	/**
	 * @param componentName the componentName to set
	 */
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	/**
	 * @return the componentVersion
	 */
	public Integer getComponentVersion() {
		return componentVersion;
	}

	/**
	 * @param componentVersion the componentVersion to set
	 */
	public void setComponentVersion(Integer componentVersion) {
		this.componentVersion = componentVersion;
	}



}
