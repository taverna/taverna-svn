package net.sf.taverna.t2.workbench.design.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * UI for creating/editing dataflow output ports.
 * 
 * @author David Withers
 */
public class DataflowOutputPortPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JTextField portNameField;

	public DataflowOutputPortPanel() {
		super(new GridBagLayout());

		portNameField = new JTextField();
 
		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.ipadx = 10;
		add(new JLabel("Name:"), constraints);

		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.ipadx = 0;
		constraints.weightx = 1d;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(portNameField, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.VERTICAL;
		constraints.weighty = 1d;
		add(new JPanel(), constraints);
	}
	
	/**
	 * Returns the portNameField.
	 *
	 * @return the portNameField
	 */
	public JTextField getPortNameField() {
		return portNameField;
	}

	/**
	 * Returns the port name.
	 *
	 * @return the port name
	 */
	public String getPortName() {
		return portNameField.getText();
	}
	
	/**
	 * Sets the port name.
	 *
	 * @param name the name of the port
	 */
	public void setPortName(String name) {
		portNameField.setText(name);
	}
	
}
