/*
 * Created on May 18, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class ResetAction extends ScuflModelAction {
	/**
	 * @param model
	 */
	private Component parentComponent;
	
	public ResetAction(final ScuflModel model,Component parentComponnt) {
		super(model);
		putValue(SMALL_ICON, TavernaIcons.deleteIcon);
		putValue(NAME, "Reset");
		putValue(SHORT_DESCRIPTION, "Reset Workflow...");
		this.parentComponent=parentComponnt;
	}

	/*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		final Object[] options = { "Reset Workflow", "Cancel" };
		final int n = JOptionPane
				.showOptionDialog(
						parentComponent,
						"Are you sure you want to reset the workflow?\nIf you haven't saved, then any changes you have\nmade will be lost.",
						"Confirm workflow reset", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if (n == 0) {
			model.clear();
		}

	}

}
