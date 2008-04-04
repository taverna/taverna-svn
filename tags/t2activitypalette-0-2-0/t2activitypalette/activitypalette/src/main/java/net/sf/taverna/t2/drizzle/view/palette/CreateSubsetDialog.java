/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.palette;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.drizzle.model.ActivityPaletteModel;

/**
 * @author alanrw
 * 
 */
public final class CreateSubsetDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4916707367626813948L;

	/**
	 * @param paletteModel
	 */
	public CreateSubsetDialog(final ActivityPaletteModel paletteModel) {
		this.setLayout(new GridLayout(3, 1));

		JPanel namePanel = new JPanel();
		namePanel.add(new JLabel("Subset name")); //$NON-NLS-1$
		final JTextField nameField = new JTextField();
		nameField.setColumns(10);
		namePanel.add(nameField);
		this.add(namePanel);

		JPanel kindPanel = new JPanel();
		kindPanel.add(new JLabel("Subset kind")); //$NON-NLS-1$

		final Set<String> subsetKinds = paletteModel.getSubsetKinds();
		final JComboBox kindCombo = new JComboBox(subsetKinds
				.toArray(new String[0]));
		kindCombo.setEditable(true);
		kindPanel.add(kindCombo);
		this.add(kindPanel);

		JPanel buttonPanel = new JPanel();
		JButton cancelButton = new JButton("Cancel"); //$NON-NLS-1$
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				CreateSubsetDialog.this.dispose();
			}

		});
		JButton createButton = new JButton("Create"); //$NON-NLS-1$
		createButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				Set<String> subsetNames = paletteModel.getSubsetNames();
				String subsetName = nameField.getText();
				if ((subsetName == null) || (subsetName.length() == 0)) {
					JOptionPane.showMessageDialog(CreateSubsetDialog.this,
							"subset name must be specified"); //$NON-NLS-1$
					return;
				}
				if (subsetNames.contains(subsetName)) {
					JOptionPane.showMessageDialog(CreateSubsetDialog.this,
							"The name " + subsetName + " is already in use"); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
				String subsetKind = (String) kindCombo.getSelectedItem();
				if ((subsetKind == null) || (subsetKind.length() == 0)) {
					JOptionPane.showMessageDialog(CreateSubsetDialog.this,
							"subset kind must be specified"); //$NON-NLS-1$
					return;
				}
				paletteModel.addSubsetModelFromUser(subsetName, subsetKind,
						!subsetKinds.contains(subsetKind));
				CreateSubsetDialog.this.dispose();
			}

		});
		buttonPanel.add(cancelButton);
		buttonPanel.add(createButton);
		this.add(buttonPanel);

		this.pack();
	}
}
