/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.Version;

/**
 * @author alanrw
 * 
 */
public class ComponentListCellRenderer implements ListCellRenderer {

	private static DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	@Override
	public java.awt.Component getListCellRendererComponent(JList list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		return defaultRenderer.getListCellRendererComponent(list,
				convertValueToString(value), index, isSelected, cellHasFocus);
	}

	public static String convertValueToString(Object value) {
		if (value instanceof Registry) {
			return ((Registry) value).getRegistryBase().toString();
		}
		if (value instanceof Family) {
			return ((Family) value).getName();
		}
		if (value instanceof Component) {
			return ((Component) value).getName();
		}
		if (value instanceof Version) {
			return ((Version) value).getVersionNumber().toString();
		}
		if (value instanceof Integer) {
			return ((Integer) value).toString();
		}
		if (value instanceof String) {
			return (String) value;
		}
		if (value == null) {
			return ("null");
		}
		return "Spaceholder for " + value.getClass().getName();
	}

}
