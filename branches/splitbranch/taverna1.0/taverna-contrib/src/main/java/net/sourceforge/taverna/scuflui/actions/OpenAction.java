package net.sourceforge.taverna.scuflui.actions;

/**
 * Represents a file open action.
 * @author Mark Fortner
 * @version $Revision: 1.1.2.2 $
 */
import javax.swing.Action;

public class OpenAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_ABOUT = "open-command";

	private static final String NAME_ABOUT = "Open...";

	private static final String SMALL_ICON_ABOUT = "etc/icons/stock_open-16.png";

	private static final String LARGE_ICON_ABOUT = "etc/icons/stock_open.png";

	private static final String SHORT_DESCRIPTION_ABOUT = "Open File";

	private static final String LONG_DESCRIPTION_ABOUT = "Open A New File";

	private static final int MNEMONIC_KEY_ABOUT = 'O';

	private static final Character ACCELERATOR_KEY = new Character('O');

	/**
	 * Constructor
	 */
	public OpenAction() {
		putValue(Action.NAME, NAME_ABOUT);
		putValue(Action.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
		putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
		putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
		putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
		putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
		putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
		putValue(Action.ACCELERATOR_KEY, getKeyStroke(ACCELERATOR_KEY));
	}

}
