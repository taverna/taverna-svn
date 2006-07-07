package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sourceforge.taverna.scuflui.workbench.Workbench;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scuflui.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.ScuflIcons;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class OpenWorkflowAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_ABOUT = "open-command";

	private static final String NAME_ABOUT = "Open Workflow...";

	private static final String SMALL_ICON_ABOUT = "org/embl/ebi/escience/scuflui/icons/generic/open.gif";

	private static final String LARGE_ICON_ABOUT = "org/embl/ebi/escience/scuflui/icons/generic/open.gif";

	private static final String SHORT_DESCRIPTION_ABOUT = "Open Workflow";

	private static final String LONG_DESCRIPTION_ABOUT = "Open Workflow";

	private static final int MNEMONIC_KEY_ABOUT = 'O';

	private static final Character ACCELERATOR_KEY = new Character('O');

	/**
	 * Constructor
	 */
	public OpenWorkflowAction() {

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
		// Load an XScufl definition here
		Preferences prefs = Preferences.userNodeForPackage(ScuflIcons.class);
		String curDir = prefs.get("currentDir", System.getProperty("user.home"));
		JFileChooser fc = new JFileChooser();
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[] { "xml" }));
		fc.setCurrentDirectory(new File(curDir));
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fc.getCurrentDirectory().toString());
			final File file = fc.getSelectedFile();
			// mrp Refactored to do the heavy-lifting in a new thread
			new Thread(new Runnable() {
				public void run() {
					try {
						// todo: does the update need running in the AWT thread?
						// perhaps this thread should be spawned in populate?

						ScuflModel model = Workbench.getModel();
						model = (model == null) ? new ScuflModel() : model;

						XScuflParser.populate(file.toURL().openStream(), model, null);
						Workbench.setModel(model);

					} catch (Exception ex) {
						ex.printStackTrace();
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