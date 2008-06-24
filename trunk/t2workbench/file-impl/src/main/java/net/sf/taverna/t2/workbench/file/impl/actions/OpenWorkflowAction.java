package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.FileTypeFileFilter;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

public class OpenWorkflowAction extends AbstractAction {

	private final class FileOpenerThread extends Thread {
		private final Component parentComponent;
		private final File[] files;
		private final FileType fileType;

		private FileOpenerThread(Component parentComponent,
				File[] selectedFiles, FileType fileType) {
			super("Opening dataflow(s) " + Arrays.asList(selectedFiles));
			this.parentComponent = parentComponent;
			this.files = selectedFiles;
			this.fileType = fileType;
		}

		@Override
		public void run() {
			openWorkflows(parentComponent, files, fileType);
		}
	}

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
		openWorkflows(parentComponent);
	}

	public boolean openWorkflows(final Component parentComponent) {
		JFileChooser fileChooser = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		fileChooser.setDialogTitle(OPEN_WORKFLOW);

		fileChooser.resetChoosableFileFilters();
		List<FileFilter> fileFilters = fileManager.getOpenFileFilters();
		if (fileFilters.isEmpty()) {
			JOptionPane.showMessageDialog(parentComponent,
					"No file types found for opening workflow.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		for (FileFilter fileFilter : fileFilters) {
			fileChooser.addChoosableFileFilter(fileFilter);
		}
		fileChooser.setFileFilter(fileFilters.get(0));

		fileChooser.setCurrentDirectory(new File(curDir));
		fileChooser.setMultiSelectionEnabled(true);

		int returnVal = fileChooser.showOpenDialog(parentComponent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fileChooser.getCurrentDirectory()
					.toString());
			final File[] selectedFiles = fileChooser.getSelectedFiles();
			FileTypeFileFilter fileFilter = (FileTypeFileFilter) fileChooser
					.getFileFilter();
			new FileOpenerThread(parentComponent, selectedFiles, fileFilter
					.getFileType()).start();
			return true;
		}
		return false;
	}

	public void openWorkflows(Component parentComponent, File[] files,
			FileType fileType) {
		for (File file : files) {
			try {
				fileManager.openDataflow(fileType, file);
			} catch (OpenException ex) {
				logger.warn("Could not open workflow from " + file, ex);
				JOptionPane.showMessageDialog(parentComponent,
						"Could not open workflow from " + file + ": \n\n"
								+ ex.getMessage(), "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
	}

}
