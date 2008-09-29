/*
 * Created on May 18, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scuflui.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.ScuflIcons;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1 $
 */
public class LoadWorkflowAction extends ScuflModelAction
{
    final JFileChooser fc = new JFileChooser();
	
	/**
	 * @param model
	 */
	public LoadWorkflowAction(ScuflModel model)
	{
		super(model);
		putValue(SMALL_ICON, ScuflIcons.openIcon);
		putValue(NAME, "Load");
		putValue(SHORT_DESCRIPTION, "Load a workflow...");
	}

	public void actionPerformed(ActionEvent e)
	{
		// Load an XScufl definition here
		Preferences prefs = Preferences.userNodeForPackage(ScuflIcons.class);
		String curDir = prefs.get("currentDir", System.getProperty("user.home"));
		fc.setDialogTitle("Open Workflow");
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[] { "xml" }));
		fc.setCurrentDirectory(new File(curDir));
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			prefs.put("currentDir", fc.getCurrentDirectory().toString());
			final File file = fc.getSelectedFile();
			// mrp Refactored to do the heavy-lifting in a new thread
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						// todo: does the update need running in the AWT thread?
						// perhaps this thread should be spawned in populate?
						XScuflParser.populate(file.toURL().openStream(), model, null);
					}
					catch (Exception ex)
					{
						JOptionPane
								.showMessageDialog(
										null,
										"Problem opening workflow from file : \n\n"
												+ ex.getMessage()
												+ "\n\nTo load this workflow try setting offline mode, this will allow you to load and remove any defunct operations.",
										"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}).start();

		}

	}

}
