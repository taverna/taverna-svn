/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;

/**
 * A container for the various icons used by the Scufl ui components
 * 
 * @author Tom Oinn
 */
public class ScuflIcons {

	public static ImageIcon classIcon, selectedClassIcon, nullIcon;

	static {
		// Load the image files found in this package into the class.
		try {
			Class c = Class.forName("org.embl.ebi.escience.scuflui.ScuflIcons");
			classIcon = new ImageIcon(c
					.getResource("icons/semantics/class.gif"));
			selectedClassIcon = new ImageIcon(c
					.getResource("icons/semantics/selectedclass.gif"));
			nullIcon = new ImageIcon(new java.awt.image.BufferedImage(1, 1,
					java.awt.image.BufferedImage.TYPE_INT_RGB));

		} catch (ClassNotFoundException cnfe) {
			//
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.toString());
		}
	}

}
