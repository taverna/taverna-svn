/**
 *
 */
package net.sf.taverna.t2.activities.apiconsumer.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivity;
import net.sf.taverna.t2.activities.apiconsumer.actions.ApiConsumerActivityConfigurationAction;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;

/**
 * @author alanrw
 *
 */
public class ConfigureApiConsumerActivityMenuAction extends
		AbstractConfigureActivityMenuAction<ApiConsumerActivity> {

	private EditManager editManager;
	private FileManager fileManager;
	private ActivityIconManager activityIconManager;

	public ConfigureApiConsumerActivityMenuAction() {
		super(ApiConsumerActivity.class);
	}

	@Override
	protected Action createAction() {
		ApiConsumerActivityConfigurationAction configAction = new ApiConsumerActivityConfigurationAction(
				findActivity(), getParentFrame(), editManager, fileManager, activityIconManager);
		configAction.putValue(Action.NAME, ApiConsumerActivityConfigurationAction.CONFIGURE_APICONSUMER_ACTIVITY);
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

}
