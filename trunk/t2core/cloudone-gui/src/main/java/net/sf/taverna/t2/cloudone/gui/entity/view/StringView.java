package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sf.taverna.t2.cloudone.gui.entity.model.LiteralModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.StringModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.StringModel;
import net.sf.taverna.t2.cloudone.gui.entity.view.LiteralView.EditAction;
import net.sf.taverna.t2.cloudone.gui.entity.view.LiteralView.OKAction;
import net.sf.taverna.t2.cloudone.gui.entity.view.LiteralView.RemoveAction;

import org.apache.log4j.Logger;

public class StringView extends EntityView<StringModel, StringModel, StringModelEvent>{
	
	private static Logger logger = Logger.getLogger(StringView.class);
	private EntityListView parentView;
	private JTextArea textArea;
	private JButton okButton;
	private JButton editButton;
	private JButton removeButton;
	private JButton browseButton;
	private OKAction okAction = new OKAction();
	private EditAction editAction = new EditAction();
	private RemoveAction removeAction = new RemoveAction();
	private BrowseAction browseAction = new BrowseAction();
	private JComboBox comboBox;
	private JPanel editPanel;
	private JPanel viewPanel;
	private StringModel model;
	
	public StringView(StringModel model, EntityListView parentView) {
		super(model, parentView);
		this.parentView = parentView;
		this.model = model;
		initialise();
	}
	
	public void initialise() {
		editPanel = new JPanel();
		setLayout(new GridBagLayout());
		// setBorder(BorderFactory.createLineBorder(Color.RED));
		// setOpaque(false);
		GridBagConstraints headerC = new GridBagConstraints();
		headerC.gridx = 0;
		headerC.gridy = 0;
		headerC.gridwidth = 2;
		headerC.anchor = GridBagConstraints.LAST_LINE_START;
		headerC.ipadx = 4;
		editPanel.add(new JLabel("<html><small>String</small></html>"), headerC);
		browseButton = new JButton(browseAction);
		editPanel.add(browseButton);
		GridBagConstraints fieldC = new GridBagConstraints();
		fieldC.gridx = 0;
		fieldC.gridy = 1;
		fieldC.weightx = 0.1;
		fieldC.fill = GridBagConstraints.HORIZONTAL;
		textArea = new JTextArea(10,30);
		editPanel.add(textArea, fieldC);
		

		GridBagConstraints buttonC = new GridBagConstraints();
		buttonC.gridy = 1;
		
		okButton = new JButton(okAction);
		editPanel.add(okButton, buttonC);
		editButton = new JButton(editAction);
		editPanel.add(editButton, buttonC);
		removeButton = new JButton(removeAction);
		editPanel.add(removeButton, buttonC);

		editAction.setEnabled(false);
		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// dataDocumentBuilder.edit(HttpRefSchemePanel.this);
			}
		});
		// By default, a new view is not editable
		setFieldsEditable(true);
		add(editPanel);
	}

	@Override
	protected JComponent createModelView(StringModel model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void placeViewComponent(JComponent view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void removeViewComponent(JComponent view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEdit(boolean editable) {
		// TODO Auto-generated method stub
		
	}
	
	private void setFieldsEditable(boolean editable) {
		textArea.setEditable(editable);
		editAction.setEnabled(!editable);
		okAction.setEnabled(editable);
		browseButton.setEnabled(editable);
	}
	
	public class EditAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public EditAction() {
			super("Edit");
		}

		public void actionPerformed(ActionEvent e) {
			setFieldsEditable(true);
		}
	}

	public class OKAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public OKAction() {
			super("OK");
		}

		public void actionPerformed(ActionEvent e) {
			try {
				setFieldsEditable(false);
			} catch (IllegalStateException ex) {
				// Warning box already shown, won't do edit(null)
			}
		}
	}

	public class RemoveAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public RemoveAction() {
			super("Remove");
		}

		public void actionPerformed(ActionEvent e) {
			model.remove();
			System.out.println("remove me");
		}
	}
	
	public class BrowseAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public BrowseAction() {
			super("Browse");
		}

		public void actionPerformed(ActionEvent e) {
			File file = chooseFile();
			textArea.setText("file:" + file.toString());
			textArea.revalidate();
		}
	}
	
	private File chooseFile() {
		JFileChooser fileChooser = new JFileChooser() {
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
		int returnValue = fileChooser.showDialog(StringView.this,
				"Select");
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			// do something with file
			return fileChooser.getSelectedFile();
		}
		return null; // User cancelled
	}
}
