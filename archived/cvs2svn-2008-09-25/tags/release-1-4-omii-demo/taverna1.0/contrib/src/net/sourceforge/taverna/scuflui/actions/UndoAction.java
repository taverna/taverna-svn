package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.undo.UndoManager;

public class UndoAction extends DefaultAction {
    
    private static final String ACTION_COMMAND_KEY_UNDO = "undo-command";
    private static final String NAME_UNDO = "Undo";
    private static final String SMALL_ICON_UNDO = "etc/icons/stock_undo-16.png";
    private static final String LARGE_ICON_UNDO = "etc/icons/stock_undo.png";
    private static final String SHORT_DESCRIPTION_UNDO = "Undo";
    private static final String LONG_DESCRIPTION_UNDO = "Undo previous action";
    private static final int MNEMONIC_KEY_UNDO = 'Z';

    
    public UndoAction() {
        putValue(NAME, NAME_UNDO);
        putValue(SMALL_ICON, getIcon(SMALL_ICON_UNDO));
        putValue(LARGE_ICON, getIcon(LARGE_ICON_UNDO));
        putValue(SHORT_DESCRIPTION, SHORT_DESCRIPTION_UNDO);
        putValue(LONG_DESCRIPTION, LONG_DESCRIPTION_UNDO);
        putValue(MNEMONIC_KEY, new Integer(MNEMONIC_KEY_UNDO));
        putValue(ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_UNDO);
    }

    public UndoAction(UndoManager undoMgr) {
        putValue(NAME, NAME_UNDO);
        putValue(SMALL_ICON, getIcon(SMALL_ICON_UNDO));
        putValue(LARGE_ICON, getIcon(LARGE_ICON_UNDO));
        putValue(SHORT_DESCRIPTION, SHORT_DESCRIPTION_UNDO);
        putValue(LONG_DESCRIPTION, LONG_DESCRIPTION_UNDO);
        putValue(MNEMONIC_KEY, new Integer(MNEMONIC_KEY_UNDO));
        putValue(ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_UNDO);
        this.undoMgr = undoMgr;
    }

    public void update() {
        boolean canUndo = undoMgr.canUndo();

        if (canUndo) {
            setEnabled(true);
            putValue(Action.NAME, undoMgr.getUndoPresentationName());
        } else {
            setEnabled(false);
            putValue(Action.NAME, "Undo");
        }
    }
    
    public void actionPerformed(ActionEvent ae){
        update();
    }

    private UndoManager undoMgr;

}