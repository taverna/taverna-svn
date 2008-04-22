package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.gui.entity.model.LiteralModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.LiteralModelEvent;

import org.apache.log4j.Logger;

/**
 * A view (in MVC terms) for a {@link Literal}. Does not handle string
 * literals, this is done by {@link StringView}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class LiteralView extends
		EntityView<LiteralModel, Object, LiteralModelEvent> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4812283598872330172L;
	private static final String BOOLEAN = Boolean.class.getSimpleName();
	private static final String FLOAT = Float.class.getSimpleName();
	private static final String DOUBLE = Double.class.getSimpleName();
	private static final String LONG = Long.class.getSimpleName();
	private static final String INTEGER = Integer.class.getSimpleName();

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LiteralView.class);
	private LiteralModel model;
	private EntityListView parentView;
	private JTextField textField;
	private JButton okButton;
	private JButton editButton;
	private JButton removeButton;
	private OKAction okAction = new OKAction();
	private EditAction editAction = new EditAction();
	private RemoveAction removeAction = new RemoveAction();
	private JComboBox comboBox;
	private JPanel editPanel;
	@SuppressWarnings("unused")
	private JPanel viewPanel;

	public LiteralView(LiteralModel model, EntityListView parentView) {
		super(model, parentView);
		this.model = model;
		this.parentView = parentView;
		initialise();
	}

	public void initialise() {
		setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Literal",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		editPanel = new JPanel();
		setLayout(new GridBagLayout());
		// setBorder(BorderFactory.createLineBorder(Color.RED));
		// setOpaque(false);
		String[] literalChoices = { INTEGER, LONG, DOUBLE, FLOAT, BOOLEAN, };
		comboBox = new JComboBox(literalChoices);
		GridBagConstraints headerC = new GridBagConstraints();
		headerC.gridx = 0;
		headerC.gridy = 0;
		headerC.gridwidth = 2;
		headerC.anchor = GridBagConstraints.LAST_LINE_START;
		headerC.ipadx = 4;
		// editPanel.add(new JLabel("<html><small>Literal</small></html>"),
		// headerC);
		editPanel.add(comboBox);
		GridBagConstraints fieldC = new GridBagConstraints();
		fieldC.gridx = 0;
		fieldC.gridy = 1;
		fieldC.weightx = 0.1;
		fieldC.fill = GridBagConstraints.HORIZONTAL;
		textField = new JTextField(20);
		textField.setMinimumSize(new Dimension(250, 20));
		editPanel.add(textField, fieldC);

		GridBagConstraints buttonC = new GridBagConstraints();
		buttonC.gridy = 1;

		okButton = new JButton(okAction);
		editPanel.add(okButton, buttonC);
		editButton = new JButton(editAction);
		editPanel.add(editButton, buttonC);
		removeButton = new JButton(removeAction);
		editPanel.add(removeButton, buttonC);

		editAction.setEnabled(false);
		textField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// dataDocumentBuilder.edit(HttpRefSchemePanel.this);
			}
		});
		textField.addActionListener(okAction);
		// By default, a new view is not editable
		setFieldsEditable(true);
		add(editPanel);
	}

	private void setFieldsEditable(boolean editable) {
		textField.setEditable(editable);
		textField.setEnabled(editable);
		editAction.setEnabled(!editable);
		okAction.setEnabled(editable);
		comboBox.setEnabled(editable);
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

	/**
	 * After entering the details and clicking OK this handles the Controller
	 * (in MVC terms) aspects
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public class OKAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public OKAction() {
			super("OK");
		}

		public void actionPerformed(ActionEvent e) {
			try {
				parentView.edit(null);
			} catch (IllegalStateException e1) {
				// warned already
			}
		}
	}

	/**
	 * Remove the literal from the view
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
	protected JComponent createModelView(Object model) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Check that the user entered value for the {@link Literal} is valid
	 */
	public void setLiteralFromField() {
		// is it a string etc
		String type = comboBox.getSelectedItem().toString();
		String value = textField.getText();
		try {
			if (type.equals(INTEGER)) {
				int result = Integer.parseInt(value);
				model.setLiteral(result);
			} else if (type.equals(LONG)) {
				long result = Long.parseLong(value);
				model.setLiteral(result);
			} else if (type.equals(DOUBLE)) {
				double result = Double.parseDouble(value);
				model.setLiteral(result);
			} else if (type.equals(FLOAT)) {
				float result = Float.parseFloat(value);
				model.setLiteral(result);
			} else if (type.equals(BOOLEAN)) {
				boolean result;
				if (value.equalsIgnoreCase("false")) {
					result = false;
				} else if (value.equalsIgnoreCase("true")) {
					result = true;
				} else {
					throw new NumberFormatException("Invalid boolean: " + value);
				}
				model.setLiteral(result);
			} else {
				// crash cos its bust
			}
		} catch (NumberFormatException e1) {
			JOptionPane.showMessageDialog(LiteralView.this, value
					+ " is not a valid " + type, "Invalid value",
					JOptionPane.WARNING_MESSAGE);
			textField.requestFocusInWindow();
			throw new IllegalStateException(e1);
		}
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
	protected void addModelView(Object literalModel) {
		textField.setText(literalModel.toString());
		comboBox.setSelectedItem(literalModel.getClass().getSimpleName());
	}

	@Override
	protected void removeModelView(Object refModel) {
		textField.setText("");
		comboBox.setSelectedIndex(0);
	}

	/**
	 * Check that the {@link Literal} is valid using
	 * {@link #setLiteralFromField()}
	 */
	@Override
	public void setEdit(boolean editable) throws IllegalStateException {
		if (!editable) {
			setLiteralFromField();
		}
		// Disable buttons and stuff
		setFieldsEditable(editable);
	}

}
