/*
 * Created on Jan 12, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.UIUtils;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision$
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
		Component parent = UIUtils.getActionEventParentWindow(e);
		String name = (String) JOptionPane.showInputDialog(parent,
				"Name for the new workflow output?", "Name required",
				JOptionPane.QUESTION_MESSAGE, null, null, "");
		if (name != null) {
			try {
				model.getWorkflowSinkProcessor().addPort(
						new InputPort(model.getWorkflowSinkProcessor(), name));
				model.forceUpdate();
			} catch (PortCreationException pce) {
				JOptionPane.showMessageDialog(parent,
						"Port creation exception : \n" + pce.getMessage(),
						"Exception!", JOptionPane.ERROR_MESSAGE);
			} catch (DuplicatePortNameException dpne) {
				JOptionPane.showMessageDialog(parent, "Duplicate name : \n"
						+ dpne.getMessage(), "Exception!",
						JOptionPane.ERROR_MESSAGE);
			}
		}

	}

}
