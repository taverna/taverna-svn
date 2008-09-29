/*
 * Created on Jan 12, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflIcons;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.2 $
 */
public class AddInputAction extends ScuflModelAction
{
	/**
	 * @param model
	 */
	public AddInputAction(ScuflModel model)
	{
		super(model);
		putValue(SMALL_ICON, ScuflIcons.inputIcon);
		putValue(NAME, "Create New Input...");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String name = (String) JOptionPane.showInputDialog(null,
				"Name for the new workflow input?", "Name required", JOptionPane.QUESTION_MESSAGE,
				null, null, "");
		if (name != null)
		{
			try
			{
				model.getWorkflowSourceProcessor().addPort(
						new OutputPort(model.getWorkflowSourceProcessor(), name));
				model.forceUpdate();
			}
			catch (PortCreationException pce)
			{
				JOptionPane.showMessageDialog(null, "Port creation exception : \n"
						+ pce.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
			}
			catch (DuplicatePortNameException dpne)
			{
				JOptionPane.showMessageDialog(null, "Duplicate name : \n" + dpne.getMessage(),
						"Exception!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}