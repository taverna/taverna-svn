package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.OpenException;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.actions.OpenWorkflowFromFileAction;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;

public class FileOpenAction extends AbstractAction {

	// TODO: Support .xml as well
	private static final String[] EXTENSIONS = new String[] { "t2flow" };
	private static final String OPEN_DATAFLOW = "Open dataflow…";
	private static Logger logger = Logger.getLogger(FileOpenAction.class);

	private FileManager fileManager = FileManager.getInstance();

	public FileOpenAction() {
		super(OPEN_DATAFLOW, WorkbenchIcons.openIcon);
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();

		Preferences prefs = Preferences
				.userNodeForPackage(OpenWorkflowFromFileAction.class);
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		fileChooser.setDialogTitle(OPEN_DATAFLOW);
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileFilter(new ExtensionFileFilter(EXTENSIONS));
		fileChooser.setCurrentDirectory(new File(curDir));
		// TODO: Find parent component
		Component parentComponent = null;
		int returnVal = fileChooser.showOpenDialog(parentComponent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fileChooser.getCurrentDirectory()
					.toString());
			final File file = fileChooser.getSelectedFile();
			final URL url;
			try {
				url = file.toURI().toURL();
			} catch (MalformedURLException ex) {
				logger.error("Malformed URL from file " + file, ex);
				return;
			}
			// TODO: Open in separate thread to avoid hanging UI
			try {
				fileManager.openDataflow(url);
			} catch (OpenException ex) {
				JOptionPane.showMessageDialog(parentComponent,
						"Could not open dataflow from " + url + ": \n\n"
								+ ex.getMessage(), "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

}
