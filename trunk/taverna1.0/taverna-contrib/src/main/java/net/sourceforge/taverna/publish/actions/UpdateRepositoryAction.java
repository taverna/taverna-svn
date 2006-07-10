package net.sourceforge.taverna.publish.actions;

import net.sourceforge.taverna.scuflui.actions.AboutAction;
import net.sourceforge.taverna.scuflui.actions.DefaultAction;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class UpdateRepositoryAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_ABOUT = "update-rep-command";

	private static final String NAME_ABOUT = "Update Repository...";

	// private static final String SMALL_ICON_ABOUT = "etc/metal/About16.gif";
	// private static final String LARGE_ICON_ABOUT = "etc/metal/About24.gif";
	private static final String SHORT_DESCRIPTION_ABOUT = "Update Repository";

	private static final String LONG_DESCRIPTION_ABOUT = "Update Repository To Publish Workflows";

	// private static final int MNEMONIC_KEY_ABOUT = 'A';

	public UpdateRepositoryAction() {
		putValue(AboutAction.NAME, NAME_ABOUT);
		// putValue(AboutAction.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
		// putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
		putValue(AboutAction.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
		putValue(AboutAction.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
		// putValue(AboutAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
		putValue(AboutAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);

	}

}
