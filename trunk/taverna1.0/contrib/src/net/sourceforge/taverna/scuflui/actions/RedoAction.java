package net.sourceforge.taverna.scuflui.actions;

import javax.swing.Action;
import javax.swing.undo.UndoManager;

/**
 * This action allows the user to undo actions.
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Kymerix</p>
 * @author mfortner
 * @version 1.0
 */
public class RedoAction
    extends DefaultAction {

  /**
   * Constructor
   */
  public RedoAction() {
    super("Redo", "etc/metal/Redo16.gif", "etc/metal/Redo32.gif", "Redo Edit", "Redo Edit");
    this.undoMgr = new UndoManager();
  }

  /**
   * Constructor
   * @param undoMgr UndoManager
   */
  public RedoAction(UndoManager undoMgr) {
    super("Redo", "etc/metal/Redo16.gif", "etc/metal/Redo32.gif", "Redo Edit", "Redo Edit");
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

  private UndoManager undoMgr;
}
