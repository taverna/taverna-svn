/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.subset;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.drizzle.util.ObjectFactory;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;

/**
 * @author alanrw
 * 
 */
public final class SelectKeysDialog extends JDialog {

	public SelectKeysDialog(final ActivitySubsetPanel activitySubsetPanel) {
		this.setLayout(new GridLayout(0, 1));

		final List<PropertyKey> sortedKeys = new ArrayList<PropertyKey>(activitySubsetPanel.getSubsetModel().getIdent()
				.getPropertyKeyProfile());
		final List<PropertyKey> usedKeys = activitySubsetPanel.getPropertyKeys();

		final List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
		
		for (PropertyKey pk : sortedKeys) {
			String keyName = pk.toString();
			JCheckBox checkBox = new JCheckBox(keyName);
			if (usedKeys.contains(pk)) {
				checkBox.setSelected(true);
			} else {
				checkBox.setSelected(false);
			}
			checkBoxes.add(checkBox);
			this.add(checkBox);
		}

		JPanel buttonPanel = new JPanel();
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				SelectKeysDialog.this.dispose();
			}

		});
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				List<PropertyKeySetting> keySettings = activitySubsetPanel.getKeySettings();
				Iterator<PropertyKey> pkIterator = sortedKeys.iterator();
				for (JCheckBox checkBox : checkBoxes) {
					PropertyKey pk = pkIterator.next();
					if (checkBox.isSelected() && !usedKeys.contains(pk)) {
						PropertyKeySetting setting = ObjectFactory
						.getInstance(PropertyKeySetting.class);
						setting.setPropertyKey(pk);
						keySettings.add(setting);
					} else if (usedKeys.contains(pk) && !checkBox.isSelected()) {
						PropertyKeySetting toRemove = null;
						for (PropertyKeySetting pks : keySettings) {
							if (pks.getPropertyKey().equals(pk)) {
								toRemove = pks;
								break;
							}
						}
						if (toRemove != null) {
							keySettings.remove(toRemove);
						}						
					}
				}
				activitySubsetPanel.setModels();
				SelectKeysDialog.this.dispose();
			}

		});
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		this.add(buttonPanel);

		this.pack();
	}
}
