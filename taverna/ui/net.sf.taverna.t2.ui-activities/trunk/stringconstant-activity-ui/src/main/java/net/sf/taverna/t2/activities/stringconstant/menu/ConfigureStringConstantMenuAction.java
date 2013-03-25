package net.sf.taverna.t2.activities.stringconstant.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.activities.stringconstant.actions.StringConstantActivityConfigurationAction;
import net.sf.taverna.t2.activities.stringconstant.servicedescriptions.StringConstantTemplateService;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;

public class ConfigureStringConstantMenuAction extends AbstractConfigureActivityMenuAction {

	private static final URI ACTIVITY_TYPE = URI.create("http://ns.taverna.org.uk/2010/activity/constant");

	private EditManager editManager;
	private FileManager fileManager;
	private ActivityIconManager activityIconManager;
	private ServiceDescriptionRegistry serviceDescriptionRegistry;

	public ConfigureStringConstantMenuAction() {
		super(ACTIVITY_TYPE);
	}

	@Override
	protected Action createAction() {
		StringConstantActivityConfigurationAction configAction = new StringConstantActivityConfigurationAction(
				findActivity(), getParentFrame(), editManager, fileManager, activityIconManager, serviceDescriptionRegistry);
		configAction.putValue(Action.NAME,
				StringConstantActivityConfigurationAction.CONFIGURE_STRINGCONSTANT);
		addMenuDots(configAction);
		return configAction;
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setActivityIconManager(ActivityIconManager activityIconManager) {
		this.activityIconManager = activityIconManager;
	}

	public void setServiceDescriptionRegistry(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

}
