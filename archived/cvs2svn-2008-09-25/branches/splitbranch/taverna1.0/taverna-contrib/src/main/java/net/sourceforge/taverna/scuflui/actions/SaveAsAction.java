package net.sourceforge.taverna.scuflui.actions;

/**
 * Represents the SaveAsAction
 * 
 * @author Mark Fortner
 * @version $Revision: 1.1.2.2 $
 */

public class SaveAsAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_ABOUT = "saveas-command";

	private static final String NAME_ABOUT = "Save As...";

	private static final String SMALL_ICON_ABOUT = "etc/icons/stock_save_as-16.png";

	private static final String LARGE_ICON_ABOUT = "etc/icons/stock_save_as.png";

	private static final String SHORT_DESCRIPTION_ABOUT = "Save the currently open file under a different name.";

	private static final String LONG_DESCRIPTION_ABOUT = "Save the currently open file under a different name.";

	private static final int MNEMONIC_KEY_ABOUT = 'S';

	/**
	 * Constructor
	 */
	public SaveAsAction() {
		putValue(SaveAsAction.NAME, NAME_ABOUT);
		putValue(SaveAsAction.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
		putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
		putValue(SaveAsAction.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
		putValue(SaveAsAction.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
		putValue(SaveAsAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
		putValue(SaveAsAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);

	}
}
