/**
 * 
 */
package net.sf.taverna.t2.component.ui.preference;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * @author alanrw
 *
 */
public class RemoteRegistryPanel extends JPanel {
	private static final String LOCATION_LABEL = "Location:";
	private static final String NAME_LABEL = "Name:";
	private static final long serialVersionUID = 8833815753329010062L;

	private JTextField registryNameField = new JTextField(20);
	private JTextField locationField = new JTextField(20);
	
	public RemoteRegistryPanel() {
		super(new GridBagLayout());

		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.anchor = WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.ipadx = 20;
		add(new JLabel(NAME_LABEL), constraints);

		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.ipadx = 0;
		constraints.weightx = 1d;
		constraints.fill = HORIZONTAL;
		add(registryNameField, constraints);
		
		constraints.gridy++;
		constraints.gridx = 0;
		constraints.ipadx = 20;
		constraints.fill = NONE;
		add(new JLabel(LOCATION_LABEL), constraints);
		
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.ipadx = 0;
		constraints.weightx = 1d;
		constraints.fill = HORIZONTAL;
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
