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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sf.taverna.t2.cloudone.gui.entity.model.LiteralModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.StringModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.StringModel;
import net.sf.taverna.t2.cloudone.gui.entity.view.LiteralView.EditAction;
import net.sf.taverna.t2.cloudone.gui.entity.view.LiteralView.OKAction;
import net.sf.taverna.t2.cloudone.gui.entity.view.LiteralView.RemoveAction;

import org.apache.log4j.Logger;

public class StringView extends
		EntityView<StringModel, String, StringModelEvent> {

	private static Logger logger = Logger.getLogger(StringView.class);
	private EntityListView parentView;
	private JTextArea textArea;
	private JButton okButton;
	private JButton editButton;
	private JButton removeButton;
	private OKAction okAction = new OKAction();
	private EditAction editAction = new EditAction();
	private RemoveAction removeAction = new RemoveAction();
	private JComboBox comboBox;
	private JPanel editPanel;
	private JPanel viewPanel;
	private StringModel model;
	private String string;

	public StringView(StringModel model, EntityListView parentView) {
		super(model, parentView);
		this.parentView = parentView;
		this.model = model;
		initialise();
	}

	public void initialise() {
		setBorder(javax.swing.BorderFactory.createTitledBorder(null, "String", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 1, 12)));
		editPanel = new JPanel();
		editPanel.setLayout(new GridBagLayout());
		// setBorder(BorderFactory.createLineBorder(Color.RED));
		// setOpaque(false);
		GridBagConstraints headerC = new GridBagConstraints();
		headerC.gridx = 0;
		headerC.gridy = 0;
		headerC.gridwidth = 4;
		headerC.anchor = GridBagConstraints.LAST_LINE_START;
		headerC.ipadx = 4;
		//editPanel.add(new JLabel("<html><small>String</small></html>"), headerC);

		GridBagConstraints fieldC = new GridBagConstraints();
		fieldC.gridx = 0;
		fieldC.gridy = 1;
		fieldC.gridwidth = 4;
		textArea = new JTextArea(6, 30);
		editPanel.add(new JScrollPane(textArea), fieldC);

		GridBagConstraints buttonC = new GridBagConstraints();
		buttonC.gridy = 2;
		buttonC.gridx = 0;
		okButton = new JButton(okAction);
		editPanel.add(okButton, buttonC);
		buttonC.gridx = GridBagConstraints.RELATIVE;
		editButton = new JButton(editAction);
		editPanel.add(editButton, buttonC);
		removeButton = new JButton(removeAction);
		editPanel.add(removeButton, buttonC);

		editAction.setEnabled(false);
		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				parentView.edit(getModel());
			}
		});
		// By default, a new view is not editable
		setFieldsEditable(true);
		add(editPanel);
	}

	@Override
	protected JComponent createModelView(String model) {
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
		if (!editable) {
			setStringFromField();
		}
		// Disable buttons and stuff
		setFieldsEditable(editable);
	}

	private void setStringFromField() {
		model.setString(textArea.getText());
	}

	private void setFieldsEditable(boolean editable) {
		textArea.setEditable(editable);
		editAction.setEnabled(!editable);
		okAction.setEnabled(editable);
		if (editable) {
			textArea.requestFocusInWindow();
		}
	}

	@Override
	protected void addModelView(String string) {
		textArea.setText(string);
	}

	@Override
	protected void removeModelView(String refModel) {
		textArea.setText("");
	}

	public class EditAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public EditAction() {
			super("Edit");
		}

		public void actionPerformed(ActionEvent e) {
			parentView.edit(getModel());
		}
	}

	public class OKAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public OKAction() {
			super("OK");
		}

		public void actionPerformed(ActionEvent e) {
			try {
				parentView.edit(null);
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
		}
	}

}
