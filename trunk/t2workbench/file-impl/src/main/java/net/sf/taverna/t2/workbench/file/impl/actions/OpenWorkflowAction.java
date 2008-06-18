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

import net.sf.taverna.t2.lang.ui.ExtensionFileFilter;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

public class OpenWorkflowAction extends AbstractAction {

	private final class FileOpenerThread extends Thread {
		private final Component parentComponent;
		private final File[] files;

		private FileOpenerThread(Component parentComponent,
				File[] selectedFiles) {
			super("Opening dataflow");
			this.parentComponent = parentComponent;
			this.files = selectedFiles;
		}

		@Override
		public void run() {
			openWorkflows(parentComponent, files);
		}
	}

	// TODO: Support .xml as well
	private static final String[] EXTENSIONS = new String[] { "t2flow" };
	private static final String OPEN_WORKFLOW = "Open workflow...";
	private static Logger logger = Logger.getLogger(OpenWorkflowAction.class);

	private FileManager fileManager = FileManager.getInstance();

	public OpenWorkflowAction() {
		super(OPEN_WORKFLOW, WorkbenchIcons.openIcon);
	}

	public void actionPerformed(ActionEvent e) {
		final Component parentComponent;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		} else {
			parentComponent = null;
		}

		JFileChooser fileChooser = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		fileChooser.setDialogTitle(OPEN_WORKFLOW);
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileFilter(new ExtensionFileFilter(EXTENSIONS));
		fileChooser.setCurrentDirectory(new File(curDir));
		fileChooser.setMultiSelectionEnabled(true);

		int returnVal = fileChooser.showOpenDialog(parentComponent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fileChooser.getCurrentDirectory()
					.toString());
			final File[] selectedFiles = fileChooser.getSelectedFiles();
			new FileOpenerThread(parentComponent, selectedFiles).start();

		}
	}
	
	public void openWorkflows(Component parentComponent, File[] files) {
		for (File file : files) {
			final URL url;
			try {
				url = file.toURI().toURL();
			} catch (MalformedURLException ex) {
				logger.error("Malformed URL from file " + file, ex);
				return;
			}
			try {
				fileManager.openDataflow(url);
			} catch (OpenException ex) {
				logger.warn("Could not open workflow from " + url,
						ex);
				JOptionPane.showMessageDialog(parentComponent,
						"Could not open workflow from " + url
								+ ": \n\n" + ex.getMessage(),
						"Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
	}

}
