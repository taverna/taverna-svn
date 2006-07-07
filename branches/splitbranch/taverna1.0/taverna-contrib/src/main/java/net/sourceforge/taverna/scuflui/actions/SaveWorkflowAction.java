package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sourceforge.taverna.scuflui.workbench.Workbench;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.AdvancedModelExplorer;
import org.embl.ebi.escience.scuflui.ExtensionFileFilter;

/**
 * This class saves the workflow currently loaded in the Workbench.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class SaveWorkflowAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_ABOUT = "save-workflow-command";

	private static final String NAME_ABOUT = "Save Workflow";

	private static final String SMALL_ICON_ABOUT = "etc/icons/stock_save-16.png";

	private static final String LARGE_ICON_ABOUT = "etc/icons/stock_save.png";

	private static final String SHORT_DESCRIPTION_ABOUT = "Save Workflow";

	private static final String LONG_DESCRIPTION_ABOUT = "Save Workflow";

	private static final int MNEMONIC_KEY_ABOUT = 'S';

	private static final Character ACCELERATOR_KEY = new Character('S');

	/**
	 * Constructor
	 */
	public SaveWorkflowAction() {

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

			Preferences prefs = Preferences.userNodeForPackage(AdvancedModelExplorer.class);
			String curDir = prefs.get("currentDir", System.getProperty("user.home"));
			JFileChooser fc = new JFileChooser();
			fc.resetChoosableFileFilters();
			fc.setFileFilter(new ExtensionFileFilter(new String[] { "xml" }));
			fc.setCurrentDirectory(new File(curDir));
			int returnVal = fc.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				prefs.put("currentDir", fc.getCurrentDirectory().toString());
				File file = fc.getSelectedFile();
				ScuflModel model = Workbench.getModel();
				XScuflView xsv = new XScuflView(model);
				PrintWriter out = new PrintWriter(new FileWriter(file));
				out.println(xsv.getXMLText());
				model.removeListener(xsv);
				out.flush();
				out.close();
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Problem saving workflow : \n" + ex.getMessage(), "Error!",
					JOptionPane.ERROR_MESSAGE);
		}
	}

}