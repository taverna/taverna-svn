/*
 * Created on Jan 12, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.4 $
 */
public class AddOutputAction extends ScuflModelAction {
	/**
	 * @param model
	 */
	public AddOutputAction(ScuflModel model) {
		super(model);
		putValue(SMALL_ICON, TavernaIcons.outputIcon);
		putValue(NAME, "Create New Output...");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String name = (String) JOptionPane.showInputDialog(null,
				"Name for the new workflow output?", "Name required",
				JOptionPane.QUESTION_MESSAGE, null, null, "");
		if (name != null) {
			try {
				model.getWorkflowSinkProcessor().addPort(
						new InputPort(model.getWorkflowSinkProcessor(), name));
				model.forceUpdate();
			} catch (PortCreationException pce) {
				JOptionPane.showMessageDialog(null,
						"Port creation exception : \n" + pce.getMessage(),
						"Exception!", JOptionPane.ERROR_MESSAGE);
			} catch (DuplicatePortNameException dpne) {
				JOptionPane.showMessageDialog(null, "Duplicate name : \n"
						+ dpne.getMessage(), "Exception!",
						JOptionPane.ERROR_MESSAGE);
			}
		}

	}

}
