package net.sourceforge.taverna.scuflui.actions;

import javax.swing.Action;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Represents the Cut Action.
 * 
 * @version $Revision: 1.1.2.2 $
 * @author mfortner
 */
public class CutAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_CUT = "cut-command";

	private static final String NAME_CUT = "Cut";

	private static final String SMALL_ICON_CUT = "etc/icons/stock_cut-16.png";

	private static final String LARGE_ICON_CUT = "etc/icons/stock_cut.png";

	private static final String SHORT_DESCRIPTION_CUT = "Cut";

	private static final String LONG_DESCRIPTION_CUT = "Remove the selected item from its current context. It is now available to be pasted elsewhere.";

	private static final int MNEMONIC_KEY_CUT = 'X';

	/**
	 * Constructor
	 */
	public CutAction() {
		super(new HTMLEditorKit.CutAction());

		putValue(PasteAction.NAME, NAME_CUT);
		putValue(PasteAction.SMALL_ICON, getIcon(SMALL_ICON_CUT));
		putValue(LARGE_ICON, getIcon(LARGE_ICON_CUT));
		putValue(PasteAction.SHORT_DESCRIPTION, SHORT_DESCRIPTION_CUT);
		putValue(PasteAction.LONG_DESCRIPTION, LONG_DESCRIPTION_CUT);
		putValue(PasteAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_CUT));
		putValue(PasteAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_CUT);

	}

	/**
	 * Constructor
	 * 
	 * @param act
	 *            Action
	 */
	public CutAction(Action act) {
		super(act);
		setName(NAME_CUT);
		setSmallIcon(SMALL_ICON_CUT);
		setLargeIcon(LARGE_ICON_CUT);
		setShortDescription(SHORT_DESCRIPTION_CUT);
		setLongDescription(LONG_DESCRIPTION_CUT);
		setMnemonic(new Integer(MNEMONIC_KEY_CUT));

	}
}
