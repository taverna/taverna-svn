package net.sourceforge.taverna.scuflui.actions;

import javax.swing.Action;
import javax.swing.text.StyledEditorKit;

/**
 * Represents the Copy Action.
 * 
 * @version 1.1 03/30/00
 * @author Mark Davidson
 */
public class CopyAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_COPY = "copy-command";

	private static final String NAME_COPY = "Copy";

	private static final String SMALL_ICON_COPY = "etc/icons/stock_copy-16.png";

	private static final String LARGE_ICON_COPY = "etc/icons/stock_copy.png";

	private static final String SHORT_DESCRIPTION_COPY = "Copy";

	private static final String LONG_DESCRIPTION_COPY = "Create a duplicate of the selected object. This duplicate is now available to be pasted elsewhere.";

	private static final int MNEMONIC_KEY_COPY = 'C';

	/**
	 * ctor
	 */
	public CopyAction() {
		super((Action) new StyledEditorKit.CopyAction());
		putValue(CopyAction.NAME, NAME_COPY);
		putValue(CopyAction.SMALL_ICON, getIcon(SMALL_ICON_COPY));
		putValue(LARGE_ICON, getIcon(LARGE_ICON_COPY));
		putValue(CopyAction.SHORT_DESCRIPTION, SHORT_DESCRIPTION_COPY);
		putValue(CopyAction.LONG_DESCRIPTION, LONG_DESCRIPTION_COPY);
		putValue(CopyAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_COPY));
		putValue(CopyAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_COPY);
	}

	public CopyAction(Action act) {
		super(act);
		putValue(CopyAction.NAME, NAME_COPY);
		putValue(CopyAction.SMALL_ICON, getIcon(SMALL_ICON_COPY));
		putValue(LARGE_ICON, getIcon(LARGE_ICON_COPY));
		putValue(CopyAction.SHORT_DESCRIPTION, SHORT_DESCRIPTION_COPY);
		putValue(CopyAction.LONG_DESCRIPTION, LONG_DESCRIPTION_COPY);
		putValue(CopyAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_COPY));
		putValue(CopyAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_COPY);

	}
}
