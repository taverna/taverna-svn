/*
 * FetaOntologyComboRenderer.java
 *
 * Created on March 17, 2005, 10:52 AM
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

/**
 * 
 * @author alperp
 */

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class FetaOntologyComboRenderer extends JLabel implements ListCellRenderer {

	public FetaOntologyComboRenderer() {
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}

	/*
	 * This method finds the image and text corresponding to the selected value
	 * and returns the label, set up to display the text and image.
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		// Get the selected index. (The index param isn't
		// always valid, so just use the value.)

		// System.out.println("%%%%%%%%%%%%%%%%%%%"+value);
		if (value instanceof FetaOntologyTermModel) {
			FetaOntologyTermModel ontoTermModel = (FetaOntologyTermModel) value;
			setText(ontoTermModel.toString());
		} else {
			setText((String) value);
		}
		int selectedIndex = index;

		if (isSelected) {
			// setBackground(new java.awt.Color(204, 153, 204));
			setBackground(new java.awt.Color(51, 51, 153));

			setForeground(list.getSelectionForeground());

		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		return this;
	}

}
