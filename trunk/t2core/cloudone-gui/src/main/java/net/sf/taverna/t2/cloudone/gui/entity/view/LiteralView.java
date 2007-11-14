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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.cloudone.gui.entity.model.LiteralModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.LiteralModelEvent;

import org.apache.log4j.Logger;

public class LiteralView extends EntityView<LiteralModel, LiteralModel, LiteralModelEvent> {

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
	private JPanel viewPanel;

	public LiteralView(LiteralModel model, EntityListView parentView) {
		super(model, parentView);
		this.model = model;
		this.parentView = parentView;
		initialise();
	}

	public void initialise() {
		editPanel = new JPanel();
		setLayout(new GridBagLayout());
		// setBorder(BorderFactory.createLineBorder(Color.RED));
		// setOpaque(false);
		String [] literalChoices ={"Integer", "Double", "Float", "boolean", "Long"};
		comboBox = new JComboBox(literalChoices);
		GridBagConstraints headerC = new GridBagConstraints();
		headerC.gridx = 0;
		headerC.gridy = 0;
		headerC.gridwidth = 2;
		headerC.anchor = GridBagConstraints.LAST_LINE_START;
		headerC.ipadx = 4;
		editPanel.add(new JLabel("<html><small>Literal</small></html>"), headerC);
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
				//is it a string etc
				String text = comboBox.getSelectedItem().toString();
				String value = textField.getText();
				if(text.equalsIgnoreCase("integer")) {
					System.out.println("int " + value);
				} else if (text.equalsIgnoreCase("double")) {
					System.out.println("double " + value);
				} else if (text.equalsIgnoreCase("float")) {
					System.out.println("float " + value);
				} else if (text.equalsIgnoreCase("boolean")){
					System.out.println("boolean " + value);
				} else if (text.equalsIgnoreCase("long")) {
					System.out.println("long " + value);
				} else {
					//crash cos its bust
				}
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
			model.removeLiteral();
			System.out.println("remove me");
		}
	}
	
	public class RemoveViewAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private final LiteralModel model;

		public RemoveViewAction(LiteralModel literalModel) {
			super("Remove");
			this.model = literalModel;
		}

		public void actionPerformed(ActionEvent e) {
			model.removeLiteral();
		}
	}

	@Override
	protected JComponent createModelView(LiteralModel model) {
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


}
