package net.sourceforge.taverna.scuflui.actions;

import javax.swing.Action;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Represents the Cut Action.
 *
 * @version $Revision: 1.1 $
 * @author  mfortner
 */
public class CutAction extends DefaultAction {

  private static final String ACTION_COMMAND_KEY_CUT = "cut-command";
  private static final String NAME_CUT = "Cut";
  private static final String SMALL_ICON_CUT = "etc/metal/Cut16.gif";
  private static final String LARGE_ICON_CUT = "etc/metal/Cut24.gif";
  private static final String SHORT_DESCRIPTION_CUT = "Cut";
  private static final String LONG_DESCRIPTION_CUT = "Remove the selected item from its current context. It is now available to be pasted elsewhere.";
  private static final int MNEMONIC_KEY_CUT = 'X';

  /**
   * Constructor
   */
  public CutAction() {
    init(new HTMLEditorKit.CutAction());

    setName(NAME_CUT);
    setSmallIcon(SMALL_ICON_CUT);
    setLargeIcon(LARGE_ICON_CUT);
    setShortDescription(SHORT_DESCRIPTION_CUT);
    setLongDescription(LONG_DESCRIPTION_CUT);
    setMnemonic(new Integer(MNEMONIC_KEY_CUT));
  }

  /**
   * Constructor
   * @param act Action
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
