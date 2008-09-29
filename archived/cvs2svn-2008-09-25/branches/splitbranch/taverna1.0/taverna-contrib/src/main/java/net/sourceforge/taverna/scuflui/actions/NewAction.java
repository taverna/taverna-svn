package net.sourceforge.taverna.scuflui.actions;

/**
 * Represents the New document action.
 * @author Mark Fortner
 * @version 1.0
 */
import javax.swing.Action;

public class NewAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_ABOUT = "new-command";

	private static final String NAME_ABOUT = "New...";

	private static final String SMALL_ICON_ABOUT = "etc/icons/stock_new-16.png";

	private static final String LARGE_ICON_ABOUT = "etc/icons/stock_new.png";

	private static final String SHORT_DESCRIPTION_ABOUT = "New File";

	private static final String LONG_DESCRIPTION_ABOUT = "Create A New File";

	private static final int MNEMONIC_KEY_ABOUT = 'N';

	private static final Character ACCELERATOR_KEY = new Character('N');

	/**
	 * ctor
	 */
	public NewAction() {

		putValue(Action.NAME, NAME_ABOUT);
		putValue(Action.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
		putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
		putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
		putValue(NewAction.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
		putValue(NewAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
		putValue(NewAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
		putValue(Action.ACCELERATOR_KEY, getKeyStroke(ACCELERATOR_KEY));
	}

}
