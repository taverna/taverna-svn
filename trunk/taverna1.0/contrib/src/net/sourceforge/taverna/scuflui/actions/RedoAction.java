package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.undo.UndoManager;

/**
 * This action allows the user to undo actions.
 *
 * @author mfortner
 * @version 1.0
 */
public class RedoAction extends DefaultAction {
    private static final String ACTION_COMMAND_KEY_REDO = "Redo-command";
    private static final String NAME_REDO = "Redo";
    private static final String SMALL_ICON_REDO = "etc/icons/stock_redo-16.png";
    private static final String LARGE_ICON_REDO = "etc/icons/stock_redo.png";
    private static final String SHORT_DESCRIPTION_REDO = "Redo";
    private static final String LONG_DESCRIPTION_REDO = "Redo previous action";
    private static final int MNEMONIC_KEY_REDO = 'Y';

  /**
   * Constructor
   */
  public RedoAction() {
    putValue(NAME, NAME_REDO);
    putValue(SMALL_ICON, getIcon(SMALL_ICON_REDO));
    putValue(LARGE_ICON, getIcon(LARGE_ICON_REDO));
    putValue(SHORT_DESCRIPTION, SHORT_DESCRIPTION_REDO);
    putValue(LONG_DESCRIPTION, LONG_DESCRIPTION_REDO);
    putValue(MNEMONIC_KEY, new Integer(MNEMONIC_KEY_REDO));
    putValue(ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_REDO);
    
    this.undoMgr = new UndoManager();
  }

  /**
   * Constructor
   * @param undoMgr UndoManager
   */
  public RedoAction(UndoManager undoMgr) {
    super("Redo", "/etc/icons/stock_redo-16.png", "/etc/icons/stock_redo-16.png", "Redo Edit", "Redo Edit");
    this.undoMgr = undoMgr;
  }

  public void update() {
    boolean canRedo = undoMgr.canRedo();

    if (canRedo) {
      setEnabled(true);
      putValue(Action.NAME, undoMgr.getRedoPresentationName());
    }
    else {
      setEnabled(false);
      putValue(Action.NAME, "Redo");
    }
  }
  
  public void actionPerformed(ActionEvent ae){
      update();
  }

  private UndoManager undoMgr;
}
