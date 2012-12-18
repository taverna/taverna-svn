/**
 * 
 */
package net.sf.taverna.t2.component.ui.preference;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.ui.DeselectingButton;

/**
 * @author alanrw
 *
 */
public class RemoteRegistryPanel extends JPanel {
	
	private final Logger logger = Logger.getLogger(RemoteRegistryPanel.class);
	
	private JTextField registryNameField = new JTextField(20);
	private JTextField locationField = new JTextField(20);
	
	public RemoteRegistryPanel() {
		super(new GridBagLayout());

		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.ipadx = 20;
		add(new JLabel("Name:"), constraints);

		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.ipadx = 0;
		constraints.weightx = 1d;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(registryNameField, constraints);
		
		constraints.gridy++;
		constraints.gridx = 0;
		constraints.ipadx = 20;
		constraints.fill = GridBagConstraints.NONE;
		add(new JLabel("Location:"), constraints);
		
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.ipadx = 0;
		constraints.weightx = 1d;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(locationField, constraints);
	}

	/**
	 * @return the registryNameField
	 */
	public JTextField getRegistryNameField() {
		return registryNameField;
	}

	/**
	 * @return the locationField
	 */
	public JTextField getLocationField() {
		return locationField;
	}

}
