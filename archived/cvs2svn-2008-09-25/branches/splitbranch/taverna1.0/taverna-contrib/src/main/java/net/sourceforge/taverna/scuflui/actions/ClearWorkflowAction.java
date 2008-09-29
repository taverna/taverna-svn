package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sourceforge.taverna.scuflui.workbench.Workbench;

/**
 * This class clears the current workflow.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class ClearWorkflowAction extends DefaultAction {
	private static final String ACTION_COMMAND_KEY_ABOUT = "clear-workflow-command";

	private static final String NAME_ABOUT = "Clear Workflow";

	private static final String SMALL_ICON_ABOUT = "org/embl/ebi/escience/scuflui/icons/generic/delete.gif";

	private static final String LARGE_ICON_ABOUT = "org/embl/ebi/escience/scuflui/icons/generic/delete.gif";

	private static final String SHORT_DESCRIPTION_ABOUT = "Clear Workflow";

	private static final String LONG_DESCRIPTION_ABOUT = "Clear Workflow";

	private static final int MNEMONIC_KEY_ABOUT = 'D';

	private static final Character ACCELERATOR_KEY = new Character('D');

	/**
	 * Constructor
	 */
	public ClearWorkflowAction() {

		putValue(Action.NAME, NAME_ABOUT);
		putValue(Action.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
		putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
		putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
		putValue(NewAction.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
		putValue(NewAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
		putValue(NewAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
		putValue(Action.ACCELERATOR_KEY, getKeyStroke(ACCELERATOR_KEY));
	}

	public void actionPerformed(ActionEvent ae) {
		Object[] options = { "Confirm reset", "Cancel" };
		int n = JOptionPane.showOptionDialog(null,
				"Are you sure you want to clear the workflow,\nany changes you have made will be lost?",
				"Confirm workflow reset", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
				options[1]);
		if (n == 0) {
			Workbench.clearModel();
		}
	}
}
