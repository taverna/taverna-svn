package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;

import net.sourceforge.taverna.scuflui.workbench.Workbench;

import org.embl.ebi.escience.scufl.parser.XScuflParser;

/**
 * This class opens a Help window.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.3 $
 */
public class HelpAction extends DefaultAction {
    private static final String ACTION_COMMAND_KEY_ABOUT = "help-command";

    private static final String NAME_ABOUT = "Help...";

    private static final String SMALL_ICON_ABOUT = "etc/icons/dialog-info-16.png";

    private static final String LARGE_ICON_ABOUT = "etc/icons/dialog-info.png";

    private static final String SHORT_DESCRIPTION_ABOUT = "Help";

    private static final String LONG_DESCRIPTION_ABOUT = "Help";

    //private static final int MNEMONIC_KEY_ABOUT = 'N';

    //private static final Character ACCELERATOR_KEY = new Character('N');

    /**
     * Constructor
     */
    public HelpAction() {

        putValue(Action.NAME, NAME_ABOUT);
        putValue(Action.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
        putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
        putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
        putValue(NewAction.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
        //putValue(NewAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
        putValue(NewAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
        //putValue(Action.ACCELERATOR_KEY, getKeyStroke(ACCELERATOR_KEY));
    }

    public void actionPerformed(ActionEvent ae) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Help");
        final JEditorPane helpPane = new JEditorPane();

        new Thread() {
            public void run() {
                try {
                    helpPane.setPage("http://taverna.sourceforge.net/manual/docs.orig.html");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        dialog.getContentPane().add(helpPane);
        dialog.setVisible(true);

    }
}