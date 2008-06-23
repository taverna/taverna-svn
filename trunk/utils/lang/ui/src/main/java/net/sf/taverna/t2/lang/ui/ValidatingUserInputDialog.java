package net.sf.taverna.t2.lang.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A user input dialog that validates the input as the user is entering the
 * input and gives feedback on why the input is invalid.
 * 
 * @author David Withers
 */
public class ValidatingUserInputDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private Set<String> invalidInputs;

	private String invalidInputsMessage;

	private String inputRegularExpression;

	private String inputRegularExpressionMessage;

	private String inputTitle;

	private String inputMessage;

	private JButton okButton;

	private JButton cancelButton;

	private JTextArea inputText;

	private JTextField inputField;

	private String defaultValue;

	private String inputValue;

	/**
	 * Constructs a new instance of ValidatingUserInputDialog.
	 * 
	 * @param invalidInputs
	 *            a set of inputs that are not valid. This is typically a set of
	 *            already used identifiers to avoid clashes. Can be an empty set
	 *            or null.
	 * @param invalidInputsMessage
	 *            the message to display if the user enters a value that is in
	 *            invalidInputs.
	 * @param inputRegularExpression
	 *            a regular expression that specifies a valid user input. Can be
	 *            null.
	 * @param inputRegularExpressionMessage
	 *            the message to display if the user enters a value that doesn't
	 *            match the inputRegularExpression.
	 * @param inputTitle
	 *            the title for the dialog.
	 * @param inputMessage
	 *            the message describing what the user should input.
	 * @param defaultValue
	 *            the default input value. Can be null.
	 */
	public ValidatingUserInputDialog(Set<String> invalidInputs,
			String invalidInputsMessage, String inputRegularExpression,
			String inputRegularExpressionMessage, String inputTitle,
			String inputMessage, String defaultValue) {
		this.invalidInputs = invalidInputs;
		this.invalidInputsMessage = invalidInputsMessage;
		this.inputRegularExpression = inputRegularExpression;
		this.inputRegularExpressionMessage = inputRegularExpressionMessage;
		this.inputTitle = inputTitle;
		this.inputMessage = inputMessage;
		this.defaultValue = defaultValue;
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(new EmptyBorder(5, 5, 0, 0));
		messagePanel.setBackground(Color.WHITE);

		add(messagePanel, BorderLayout.NORTH);

		JLabel inputLabel = new JLabel(inputTitle);
		inputLabel.setBackground(Color.WHITE);
		inputLabel.setFont(inputLabel.getFont().deriveFont(Font.BOLD));
		messagePanel.add(inputLabel, BorderLayout.NORTH);

		inputText = new JTextArea(inputMessage);
		inputText.setMargin(new Insets(5, 10, 10, 10));
		inputText.setMinimumSize(new Dimension(0, 30));
		inputText.setFont(inputText.getFont().deriveFont(11f));
		inputText.setEditable(false);
		messagePanel.add(inputText, BorderLayout.CENTER);

		JPanel inputPanel = new JPanel(new BorderLayout());
		inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(inputPanel, BorderLayout.CENTER);

		inputField = new JTextField();
		inputField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
			}

			public void insertUpdate(DocumentEvent e) {
				verify();
			}

			public void removeUpdate(DocumentEvent e) {
				verify();
			}
		});
		inputField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (okButton.isEnabled() && e.getKeyCode() == KeyEvent.VK_ENTER) {
					okButton.doClick();
				}
			}
		});
		inputPanel.add(inputField, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		add(buttonPanel, BorderLayout.SOUTH);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		buttonPanel.add(cancelButton);

		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inputValue = inputField.getText();
				setVisible(false);
			}
		});
		okButton.setEnabled(false);
		buttonPanel.add(okButton);

		setModal(true);
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		setSize(new Dimension(400, 200));

		setFocusTraversalPolicy(new FocusTraversalPolicy() {

			@Override
			public Component getComponentAfter(Container container,
					Component component) {
				if (component == inputField) {
					if (okButton.isEnabled()) {
						return okButton;
					} else {
						return cancelButton;
					}
				} else if (component == okButton) {
					return cancelButton;
				} else {
					return inputField;
				}
			}

			@Override
			public Component getComponentBefore(Container container,
					Component component) {
				if (component == inputField) {
					return cancelButton;
				} else if (component == okButton) {
					return inputField;
				} else {
					if (okButton.isEnabled()) {
						return okButton;
					} else {
						return inputField;
					}
				}
			}

			@Override
			public Component getDefaultComponent(Container container) {
				return inputField;
			}

			@Override
			public Component getFirstComponent(Container container) {
				return inputField;
			}

			@Override
			public Component getLastComponent(Container container) {
				return cancelButton;
			}

		});

		if (defaultValue != null) {
			inputField.setText(defaultValue);
		}

	}

	private void verify() {
		if (invalidInputs != null
				&& invalidInputs.contains(inputField.getText())) {
			inputText.setText(invalidInputsMessage);
			okButton.setEnabled(false);
			okButton.setSelected(false);
		} else if (inputRegularExpression != null
				&& !inputField.getText().matches(inputRegularExpression)) {
			inputText.setText(inputRegularExpressionMessage);
			okButton.setEnabled(false);
			okButton.setSelected(false);
		} else {
			inputText.setText(inputMessage);
			okButton.setEnabled(true);
			okButton.setSelected(true);
		}
	}

	/**
	 * Show the dialog relative to the component. If the component is null then
	 * the dialog is shown in the centre of the screen.
	 * 
	 * The value input by the user is returned. If the user clicks cancel or
	 * closes the window null is returned.
	 * 
	 * @param component
	 *            the component that the dialog is shown relative to
	 * @return the value input by the user
	 */
	public String show(Component component) {
		setLocationRelativeTo(component);
		setVisible(true);
		dispose();
		return inputValue;
	}

}
