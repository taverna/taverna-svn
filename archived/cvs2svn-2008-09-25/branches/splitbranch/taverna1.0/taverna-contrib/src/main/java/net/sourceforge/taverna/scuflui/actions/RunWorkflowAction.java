package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sourceforge.taverna.scuflui.workbench.Workbench;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scuflui.DataThingConstructionPanel;
import org.embl.ebi.escience.scuflui.EnactorInvocation;
import org.embl.ebi.escience.scuflui.UIUtils;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class RunWorkflowAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_ABOUT = "run-workflow-command";

	private static final String NAME_ABOUT = "Run Workflow";

	private static final String SMALL_ICON_ABOUT = "org/embl/ebi/escience/scuflui/icons/windows/run.gif";

	private static final String LARGE_ICON_ABOUT = "org/embl/ebi/escience/scuflui/icons/windows/run.gif";

	private static final String SHORT_DESCRIPTION_ABOUT = "Run Workflow";

	private static final String LONG_DESCRIPTION_ABOUT = "Run Workflow";

	private static final int MNEMONIC_KEY_ABOUT = 'R';

	private static final Character ACCELERATOR_KEY = new Character('R');

	/**
	 * Constructor
	 */
	public RunWorkflowAction() {

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
		final ScuflModel theModel = Workbench.getModel();
		// Check whether we're in offline mode, if so then just chuck up an
		// error
		if (theModel.isOffline()) {
			JOptionPane
					.showMessageDialog(null, "Workflow is currently offline, cannot be invoked.\n"
							+ "Deselect the 'offline' checkbox in the AME to set \n"
							+ "online mode in order to run this workflow.", "Offline, cannot invoke",
							JOptionPane.ERROR_MESSAGE);
		} else {
			if (theModel.getWorkflowSourcePorts().length != 0) {
				DataThingConstructionPanel thing = new DataThingConstructionPanel() {
					public void launchEnactorDisplay(Map inputObject) {
						try {
							UIUtils.createFrame(theModel, new EnactorInvocation(FreefluoEnactorProxy.getInstance(),
									theModel, inputObject), 100, 100, 600, 400);
						} catch (WorkflowSubmissionException wse) {
							JOptionPane.showMessageDialog(null, "Problem invoking workflow engine : \n"
									+ wse.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
						}
					}
				};
				UIUtils.createFrame(theModel, thing, 100, 100, 600, 400);
			} else {
				try {
					// No inputs so launch the enactor directly
					UIUtils.createFrame(theModel, new EnactorInvocation(FreefluoEnactorProxy.getInstance(), theModel,
							new HashMap()), 100, 100, 600, 400);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, ex.getMessage());
				}
			}

		}

	}
}
