package net.sf.taverna.t2.component.ui.serviceprovider;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.preference.ComponentPreference;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.log4j.Logger;

public class ComponentServiceDesc extends
		ServiceDescription<ComponentActivityConfigurationBean> {
	private static ComponentPreference preference = ComponentPreference
			.getInstance();
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ComponentServiceDesc.class);

	private Version.ID identification;

	public ComponentServiceDesc(Version.ID identification) {
		this.identification = identification;
	}

	/**
	 * The subclass of Activity which should be instantiated when adding a
	 * service for this description
	 */
	@Override
	public Class<? extends Activity<ComponentActivityConfigurationBean>> getActivityClass() {
		return ComponentActivity.class;
	}

	/**
	 * The configuration bean which is to be used for configuring the
	 * instantiated activity. Making this bean will typically require some of
	 * the fields set on this service description, like an endpoint URL or
	 * method name.
	 * 
	 */
	@Override
	public ComponentActivityConfigurationBean getActivityConfiguration() {
		return new ComponentActivityConfigurationBean(getIdentification());
	}

	/**
	 * An icon to represent this service description in the service palette.
	 */
	@Override
	public Icon getIcon() {
		return ComponentServiceIcon.getIcon();
	}

	/**
	 * The display name that will be shown in service palette and will be used
	 * as a template for processor name when added to workflow.
	 */
	@Override
	public String getName() {
		return getIdentification().getComponentName();
	}

	/**
	 * The path to this service description in the service palette. Folders will
	 * be created for each element of the returned path.
	 */
	@Override
	public List<String> getPath() {
		// For deeper paths you may return several strings
		return Arrays.asList("Components",
				preference.getRegistryName(identification.getRegistryBase()),
				identification.getFamilyName());
	}

	/**
	 * Return a list of data values uniquely identifying this service
	 * description (to avoid duplicates). Include only primary key like fields,
	 * ie. ignore descriptions, icons, etc.
	 */
	@Override
	protected List<? extends Object> getIdentifyingData() {
		// FIXME: Use your fields instead of example fields
		return Arrays.<Object> asList(identification.getRegistryBase(),
				identification.getFamilyName(),
				identification.getComponentName());
	}

	@Override
	public String toString() {
		return "Component " + getName();
	}

	/**
	 * @return the identification
	 */
	public Version.ID getIdentification() {
		return identification;
	}

	/**
	 * @param identification
	 *            the identification to set
	 */
	public void setIdentification(Version.ID identification) {
		this.identification = identification;
	}
}
