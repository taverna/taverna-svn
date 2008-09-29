package net.sourceforge.taverna.scuflui.actions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JDialog;

import net.sourceforge.taverna.scuflui.workbench.Workbench;
import net.sourceforge.taverna.scuflui.workbench.WorkflowHelpPanel;

/**
 * This class generates the workflow documentation for a given directory.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.3 $
 */
public class GenerateWorkflowDocAction extends DefaultAction {
    private static final String ACTION_COMMAND_KEY_ABOUT = "clear-workflow-command";

    private static final String NAME_ABOUT = "Generate Workflow Documentation";
    private static final String SMALL_ICON_ABOUT = "etc/icons/gnome-mime-manpage24.png";
    private static final String LARGE_ICON_ABOUT = "etc/icons/gnome-mime-manpage.png";
    private static final String SHORT_DESCRIPTION_ABOUT = "Generate Workflow Doc";
    private static final String LONG_DESCRIPTION_ABOUT = "Generate Workflow Documentation in HTML for a given directory";
    private static final int MNEMONIC_KEY_ABOUT = 'D';
    private static final Character ACCELERATOR_KEY =  new Character('D');

    /**
     * Constructor
     */
    public GenerateWorkflowDocAction() {

        putValue(Action.NAME, NAME_ABOUT);
        putValue(Action.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
        putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
        putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
        putValue(NewAction.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
        putValue(NewAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
        putValue(NewAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
        putValue(Action.ACCELERATOR_KEY, getKeyStroke(ACCELERATOR_KEY));
    }
    
    public void actionPerformed(ActionEvent ae){
    	JDialog dialog = new JDialog();
    	dialog.getContentPane().setLayout(new BorderLayout());
    	
    	WorkflowHelpPanel panel = new WorkflowHelpPanel();    	
    	panel.convert2Html(Workbench.getModel());
    	dialog.getContentPane().add(panel,BorderLayout.CENTER);
    	dialog.setVisible(true);
    	
    }
}
