/*
 * Created on May 18, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflIcons;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1 $
 */
public class ResetAction extends ScuflModelAction
{
	/**
	 * @param model
	 */
	public ResetAction(ScuflModel model)
	{
		super(model);
		putValue(SMALL_ICON, ScuflIcons.deleteIcon);
		putValue(NAME, "Reset");
		putValue(SHORT_DESCRIPTION, "Reset Workflow...");
	}

	/*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		Object[] options = { "Reset Workflow", "Cancel" };
		int n = JOptionPane
				.showOptionDialog(
						null,
						"Are you sure you want to reset the workflow?\nIf you haven't saved, then any changes you have\nmade will be lost.",
						"Confirm workflow reset", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if (n == 0)
		{
			model.clear();
		}

	}

}
