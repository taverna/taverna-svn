package net.sf.taverna.zaria;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * Simple ImageIcon cache for Zaria
 * 
 * @author Tom Oinn
 */
public class ZIcons {

	private static Map<String, ImageIcon> iconMap = new HashMap<String, ImageIcon>();

	public static ImageIcon iconFor(String name) {
		if (iconMap.containsKey(name)) {
			return iconMap.get(name);
		} else {
			URL iconLocation = ZIcons.class.getResource("icons/" + name
					+ ".png");
			// Prefer .png icons but use .gif if available
			if (iconLocation == null) {
				iconLocation = ZIcons.class.getResource("icons/" + name
						+ ".gif");
			}
			if (iconLocation != null) {
				ImageIcon icon = new ImageIcon(iconLocation);
				iconMap.put(name, icon);
				return icon;
			} else {
				return null;
			}
		}
	}

}
