/*
 * Created on Jan 12, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.UIUtils;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.4 $
 */
public class AddInputAction extends ScuflModelAction {
	/**
	 * @param model
	 */
	public AddInputAction(ScuflModel model) {
		super(model);
		putValue(SMALL_ICON, TavernaIcons.inputIcon);
		putValue(NAME, "Create New Input...");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {		
		Component parent = UIUtils.getActionEventParentWindow(e);
		
		String name = (String) JOptionPane.showInputDialog(parent,
				"Name for the new workflow input?", "Name required",
				JOptionPane.QUESTION_MESSAGE, null, null, "");
		if (name != null) {
			try {
				model.getWorkflowSourceProcessor()
						.addPort(
								new OutputPort(model
										.getWorkflowSourceProcessor(), name));
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