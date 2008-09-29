/*
 * Created on May 18, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scuflui.ScuflIcons;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.2 $
 */
public class LoadWebWorkflowAction extends ScuflModelAction
{

	public LoadWebWorkflowAction(ScuflModel model)
	{
		super(model);
		putValue(SMALL_ICON, ScuflIcons.openurlIcon);
		putValue(NAME, "Load from URL");
		putValue(SHORT_DESCRIPTION, "Load a workflow from a URL...");
	}

	/*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			String name = (String) JOptionPane.showInputDialog(null,
					"Enter the URL of a workflow definition to load", "Workflow URL",
					JOptionPane.QUESTION_MESSAGE, null, null, "http://");
			if (name != null)
			{
				XScuflParser.populate((new URL(name)).openStream(), model, null);
			}
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null, "Problem opening workflow from web : \n"
					+ ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
		}
	}
}