package net.sourceforge.taverna.scuflui.actions;

/**
 * Represents the Save Action.
 * 
 * @author Mark Fortner
 * @version $Revision: 1.1.2.2 $
 */

public class SaveAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_ABOUT = "save-command";

	private static final String NAME_ABOUT = "Save...";

	private static final String SMALL_ICON_ABOUT = "etc/icons/stock_save-16.png";

	private static final String LARGE_ICON_ABOUT = "etc/icons/stock_save.png";

	private static final String SHORT_DESCRIPTION_ABOUT = "Save the currently open file.";

	private static final String LONG_DESCRIPTION_ABOUT = "Save the currently open file.";

	private static final int MNEMONIC_KEY_ABOUT = 'S';

	/**
	 * ctor
	 */
	public SaveAction() {
		putValue(SaveAction.NAME, NAME_ABOUT);
		putValue(SaveAction.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
		putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
		putValue(SaveAction.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
		putValue(SaveAction.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
		putValue(SaveAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
		putValue(SaveAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
	}
}
