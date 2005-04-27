package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;

/**
 * This class imports a workflow into the cuccrent workflow.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class ImportWorkflowAction extends DefaultAction{

    private static final String ACTION_COMMAND_KEY_ABOUT = "import-workflow-command";
    private static final String NAME_ABOUT = "Import Workflow...";
    private static final String SMALL_ICON_ABOUT = "org/embl/ebi/escience/scuflui/icons/generic/import.gif";
    private static final String LARGE_ICON_ABOUT = "org/embl/ebi/escience/scuflui/icons/generic/import.gif";
    private static final String SHORT_DESCRIPTION_ABOUT = "Import workflow into current workflow";
    private static final String LONG_DESCRIPTION_ABOUT = "Import a workflow into the currently open workflow";
    private static final int MNEMONIC_KEY_ABOUT = 'I';

    /**
     * Constructor
     */
    public ImportWorkflowAction() {
        putValue(ImportWorkflowAction.NAME, NAME_ABOUT);
        putValue(ImportWorkflowAction.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
        putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
        putValue(AboutAction.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
        putValue(AboutAction.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
        putValue(AboutAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
        putValue(AboutAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
    }
    
    public void actionPerformed(ActionEvent ae){
       //TODO: implement me 
    }

}
