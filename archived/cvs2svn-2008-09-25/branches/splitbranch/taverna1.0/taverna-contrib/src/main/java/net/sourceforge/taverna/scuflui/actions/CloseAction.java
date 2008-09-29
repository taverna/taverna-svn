package net.sourceforge.taverna.scuflui.actions;

/**
 * This action closes a document.
 * 
 * @author Mark Fortner
 * @version $Revision: 1.1.2.2 $
 */

public class CloseAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_ABOUT = "close-command";

	private static final String NAME_ABOUT = "Close";

	private static final String SMALL_ICON_ABOUT = "etc/win/closeFile.gif";

	private static final String LARGE_ICON_ABOUT = "etc/win/closeFile.gif";

	private static final String SHORT_DESCRIPTION_ABOUT = "Close the currently open file";

	private static final String LONG_DESCRIPTION_ABOUT = "Close the currently open file";

	private static final int MNEMONIC_KEY_ABOUT = 'W';

	/**
	 * ctor
	 */
	public CloseAction() {
		putValue(CloseAction.NAME, NAME_ABOUT);
		putValue(CloseAction.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
		putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
		putValue(CloseAction.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
		putValue(CloseAction.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
		putValue(CloseAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
		putValue(CloseAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
	}

}
