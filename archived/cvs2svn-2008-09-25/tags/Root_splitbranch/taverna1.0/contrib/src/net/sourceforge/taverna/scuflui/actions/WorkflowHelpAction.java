package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;

/**
 * This class launches the workflow help window.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class WorkflowHelpAction extends DefaultAction {

    private static final String ACTION_COMMAND_KEY_ABOUT = "workflow-help-command";
    private static final String NAME_ABOUT = "Workflow Help...";
    private static final String SMALL_ICON_ABOUT = "etc/icons/stock_help-16.png";
    private static final String LARGE_ICON_ABOUT = "etc/icons/stock_help.png";
    private static final String SHORT_DESCRIPTION_ABOUT = "Get help regarding a workflow";
    private static final String LONG_DESCRIPTION_ABOUT = "Provide information regarding the application";
    private static final int MNEMONIC_KEY_ABOUT = 'H';

    /**
     * Constructor
     */
    public WorkflowHelpAction() {
        putValue(AboutAction.NAME, NAME_ABOUT);
        putValue(AboutAction.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
        putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
        putValue(AboutAction.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
        putValue(AboutAction.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
        putValue(AboutAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
        putValue(AboutAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
    }
    
    public void actionPerformed(ActionEvent ae){
        
    }

}
