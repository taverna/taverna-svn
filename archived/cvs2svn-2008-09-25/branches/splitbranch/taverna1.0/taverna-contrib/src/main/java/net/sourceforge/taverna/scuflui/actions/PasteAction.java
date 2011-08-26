package net.sourceforge.taverna.scuflui.actions;

import javax.swing.Action;
import javax.swing.text.StyledEditorKit;

/**
 * Represents the Paste Action.
 * 
 * @version $Revision: 1.1.2.2 $
 * @author mfortner
 */
public class PasteAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_PASTE = "paste-command";

	private static final String NAME_PASTE = "Paste";

	private static final String SMALL_ICON_PASTE = "etc/icons/stock_paste-16.png";

	private static final String LARGE_ICON_PASTE = "etc/icons/stock_paste.png";

	private static final String SHORT_DESCRIPTION_PASTE = "Paste";

	private static final String LONG_DESCRIPTION_PASTE = "Insert an object or data previously selected via \"Copy\" or \"Cut\"";

	private static final int MNEMONIC_KEY_PASTE = 'P';

	/**
	 * Constructor
	 */
	public PasteAction() {
		super((Action) new StyledEditorKit.PasteAction());
		putValue(PasteAction.NAME, NAME_PASTE);
		putValue(PasteAction.SMALL_ICON, getIcon(SMALL_ICON_PASTE));
		putValue(LARGE_ICON, getIcon(LARGE_ICON_PASTE));
		putValue(PasteAction.SHORT_DESCRIPTION, SHORT_DESCRIPTION_PASTE);
		putValue(PasteAction.LONG_DESCRIPTION, LONG_DESCRIPTION_PASTE);
		putValue(PasteAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_PASTE));
		putValue(PasteAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_PASTE);
	}

	/**
	 * 
	 * @param act
	 *            Action
	 */
	public PasteAction(Action act) {
		init(act);
		init(NAME_PASTE, SMALL_ICON_PASTE, LARGE_ICON_PASTE, LONG_DESCRIPTION_PASTE, SHORT_DESCRIPTION_PASTE);
	}
}
