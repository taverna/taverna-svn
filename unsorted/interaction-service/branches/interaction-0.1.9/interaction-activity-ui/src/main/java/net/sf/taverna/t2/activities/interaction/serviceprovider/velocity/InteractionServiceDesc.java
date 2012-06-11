package net.sf.taverna.t2.activities.interaction.serviceprovider.velocity;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.InteractionActivityConfigurationBean;
import net.sf.taverna.t2.activities.interaction.InteractionActivityType;
import net.sf.taverna.t2.activities.interaction.serviceprovider.InteractionServiceIcon;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;


public class InteractionServiceDesc extends ServiceDescription<InteractionActivityConfigurationBean> {
	

	/**
	 * The subclass of Activity which should be instantiated when adding a service
	 * for this description 
	 */
	@Override
	public Class<? extends Activity<InteractionActivityConfigurationBean>> getActivityClass() {
		return InteractionActivity.class;
	}

	/**
	 * The configuration bean which is to be used for configuring the instantiated activity.
	 * Making this bean will typically require some of the fields set on this service
	 * description, like an endpoint URL or method name. 
	 * 
	 */
	@Override
	public InteractionActivityConfigurationBean getActivityConfiguration() {
		InteractionActivityConfigurationBean bean = new InteractionActivityConfigurationBean();
		bean.setPresentationOrigin(templateName);
		bean.setInteractionActivityType(InteractionActivityType.VelocityTemplate);
		return bean;
	}

	/**
	 * An icon to represent this service description in the service palette.
	 */
	@Override
	public Icon getIcon() {
		return InteractionServiceIcon.getIcon();
	}

	/**
	 * The display name that will be shown in service palette and will
	 * be used as a template for processor name when added to workflow.
	 */
	@Override
	public String getName() {
		return templateName;
	}

	/**
	 * The path to this service description in the service palette. Folders
	 * will be created for each element of the returned path.
	 */
	@Override
	public List<String> getPath() {
		// For deeper paths you may return several strings
		return Arrays.asList("Interaction");
	}

	/**
	 * Return a list of data values uniquely identifying this service
	 * description (to avoid duplicates). Include only primary key like fields,
	 * ie. ignore descriptions, icons, etc.
	 */
	@Override
	protected List<? extends Object> getIdentifyingData() {
		// FIXME: Use your fields instead of example fields
		return Arrays.<Object>asList(templateName);
	}

	
	private String templateName;


	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}


}
