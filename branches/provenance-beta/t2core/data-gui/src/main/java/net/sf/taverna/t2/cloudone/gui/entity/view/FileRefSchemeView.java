package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.sf.taverna.t2.cloudone.gui.entity.model.FileRefSchemeModel;
import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

/**
 * A View (in MVC terms) representing a {@link FileReferenceScheme}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class FileRefSchemeView extends RefSchemeView implements Observer<File> {

	private static final long serialVersionUID = 1L;
	private FileRefSchemeModel model;
	private DataDocumentEditView parentView;
	private JTextField textField;
	private JButton browseButton;
	private BrowseAction browseAction = new BrowseAction();
	private RemoveAction removeAction = new RemoveAction();

	/**
	 * Link the {@link FileRefSchemeView} with it's {@link FileRefSchemeModel}
	 * and register for event notifications
	 * 
	 * @param model
	 * @param parentView
	 */
	public FileRefSchemeView(FileRefSchemeModel model,
			DataDocumentEditView parentView) {
		this.model = model;
		this.parentView = parentView;
		model.addObserver(this);
		initialise();

	}

	private void initialise() {
		// GUI stuff
		setBorder(javax.swing.BorderFactory.createTitledBorder(null,
				"File Reference Scheme",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		setLayout(new GridBagLayout());
		// setBorder(BorderFactory.createLineBorder(Color.GREEN));
		// setOpaque(false);
		GridBagConstraints headerC = new GridBagConstraints();
		headerC.gridx = 0;
		headerC.gridy = 0;
		headerC.gridwidth = 2;
		headerC.anchor = GridBagConstraints.LAST_LINE_START;
		headerC.ipadx = 4;
		// add(new JLabel("<html><small>File reference</small></html>"),
		// headerC);

		GridBagConstraints fieldConstraints = new GridBagConstraints();
		fieldConstraints.gridx = 0;
		fieldConstraints.gridy = 1;
		fieldConstraints.weightx = 0.1;
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		textField = new JTextField(20);
		textField.setMinimumSize(new Dimension(250, 20));
		textField.setEditable(false);
		add(textField, fieldConstraints);
		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.gridy = 1;

		browseButton = new JButton(browseAction);
		add(browseButton, buttonConstraints);
		JButton removeButton = new JButton(removeAction);
		add(removeButton, buttonConstraints);
	}

	/**
	 * What local {@link File} does this {@link FileRefSchemeView} and
	 * {@link FileRefSchemeModel} represent
	 * 
	 * @param selectedFile
	 * @return
	 */
	public File chooseFile(File selectedFile) {
		JFileChooser fileChooser = new JFileChooser() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -1764126995351013988L;

			@Override
			public void approveSelection() {
				File file = getSelectedFile();
				if (!file.isFile()) {
					JOptionPane.showMessageDialog(this, file
							+ " is not a valid file", "Invalid file",
							JOptionPane.WARNING_MESSAGE);
				} else {
					super.approveSelection();
				}
			}
		};
		fileChooser.setSelectedFile(selectedFile);
		int returnValue = fileChooser.showDialog(FileRefSchemeView.this,
				"Select");
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			// do something with file
			return fileChooser.getSelectedFile();
		}
		return null; // User cancelled
	}

	public void notify(Observable<File> sender, File file) {
		String path = "";
		if (file != null) {
			path = file.getAbsolutePath();
		}
		textField.setText(path);
	}

	/**
	 * Switch on editing of the file
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public class BrowseAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public BrowseAction() {
			super("Browse");
		}

		public void actionPerformed(ActionEvent e) {
			parentView.edit(model);
		}
	}

	/**
	 * Remove the view and the model
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public class RemoveAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public RemoveAction() {
			super("Remove");
		}

		public void actionPerformed(ActionEvent e) {
			model.remove();
		}
	}

	@Override
	public void setEdit(boolean editable) throws IllegalStateException {
		if (editable) {
			File file = chooseFile(model.getFile());
			if (file != null) {
				model.setFile(file);
			}
		}
	}

}
