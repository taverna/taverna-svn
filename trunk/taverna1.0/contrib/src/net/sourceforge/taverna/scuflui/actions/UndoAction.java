package net.sourceforge.taverna.scuflui.actions;

import javax.swing.Action;
import javax.swing.undo.UndoManager;

public class UndoAction extends DefaultAction{
  public UndoAction() {
    super("Undo", "etc/metal/Undo16.gif","etc/metal/Undo24.gif", "Undo Edit","Undo Edit");
  }

  public UndoAction(UndoManager undoMgr){
    super("Undo", "/etc/metal/Undo16.gif","etc/metal/Undo24.gif", "Undo Edit","Undo Edit");
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
