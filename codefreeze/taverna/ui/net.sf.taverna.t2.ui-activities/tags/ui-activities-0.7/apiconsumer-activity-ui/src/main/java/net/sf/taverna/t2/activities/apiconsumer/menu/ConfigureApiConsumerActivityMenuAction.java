/**
 * 
 */
package net.sf.taverna.t2.activities.apiconsumer.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivity;
import net.sf.taverna.t2.activities.apiconsumer.actions.ApiConsumerActivityConfigurationAction;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;

/**
 * @author alanrw
 *
 */
public class ConfigureApiConsumerActivityMenuAction extends
		AbstractConfigureActivityMenuAction<ApiConsumerActivity> {

	private static final String CONFIGURE_APICONSUMER_ACTIVITY = "Configure Api Consumer";

	public ConfigureApiConsumerActivityMenuAction() {
		super(ApiConsumerActivity.class);
	}
	
	@Override
	protected Action createAction() {
		ApiConsumerActivityConfigurationAction configAction = new ApiConsumerActivityConfigurationAction(
				findActivity(), getParentFrame());
		configAction.putValue(Action.NAME, CONFIGURE_APICONSUMER_ACTIVITY);
		addMenuDots(configAction);
		return configAction;
	}

}
