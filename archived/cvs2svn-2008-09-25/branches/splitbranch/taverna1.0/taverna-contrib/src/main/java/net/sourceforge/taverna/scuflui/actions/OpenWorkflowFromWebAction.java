package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sourceforge.taverna.scuflui.workbench.Workbench;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;

/**
 * This class opens a workflow from a web site.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class OpenWorkflowFromWebAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_ABOUT = "open-wkflow-from-web-command";

	private static final String NAME_ABOUT = "Open Workflow From Web...";

	private static final String SMALL_ICON_ABOUT = "org/embl/ebi/escience/scuflui/icons/generic/web.gif";

	private static final String LARGE_ICON_ABOUT = "org/embl/ebi/escience/scuflui/icons/generic/web.gif";

	private static final String SHORT_DESCRIPTION_ABOUT = "Open Workflow From Web";

	private static final String LONG_DESCRIPTION_ABOUT = "Open Workflow From Web";

	private static final int MNEMONIC_KEY_ABOUT = 'N';

	private static final Character ACCELERATOR_KEY = new Character('N');

	/**
	 * Constructor
	 */
	public OpenWorkflowFromWebAction() {

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
		try {
			String name = (String) JOptionPane.showInputDialog(null, "URL of an workflow definition to open?",
					"URL Required", JOptionPane.QUESTION_MESSAGE, null, null, "http://");
			if (name != null) {
				ScuflModel model = new ScuflModel();
				XScuflParser.populate((new URL(name)).openStream(), model, null);
				Workbench.setModel(model);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Problem opening workflow from web : \n" + ex.getMessage(), "Error!",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}