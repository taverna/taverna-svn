package net.sf.taverna.t2.workbench.design.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * UI for editing processors.
 * 
 * @author David Withers
 */
public class ProcessorPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JTextField processorNameField;

	public ProcessorPanel() {
		super(new GridBagLayout());

		processorNameField = new JTextField();
 
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
		add(processorNameField, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.VERTICAL;
		constraints.weighty = 1d;
		add(new JPanel(), constraints);
	}
	
	/**
	 * Returns the processorNameField.
	 *
	 * @return the processorNameField
	 */
	public JTextField getProcessorNameField() {
		return processorNameField;
	}

	/**
	 * Returns the processor name.
	 *
	 * @return the processor name
	 */
	public String getProcessorName() {
		return processorNameField.getText();
	}
	
	/**
	 * Sets the processor name.
	 *
	 * @param name the name of the processor
	 */
	public void setProcessorName(String name) {
		processorNameField.setText(name);
	}
	
}
