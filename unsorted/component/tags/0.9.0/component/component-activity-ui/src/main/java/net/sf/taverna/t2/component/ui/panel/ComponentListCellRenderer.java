/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentVersion;

/**
 * @author alanrw
 *
 */
public class ComponentListCellRenderer implements ListCellRenderer {

	private static DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	@Override
	public java.awt.Component getListCellRendererComponent(JList list,
			Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		return defaultRenderer.getListCellRendererComponent(list, convertValueToString(value), index, isSelected, cellHasFocus);
	}

	public static String convertValueToString(Object value) {
		if (value instanceof ComponentRegistry) {
			return ((ComponentRegistry) value).getRegistryBase().toString();
		}
		if (value instanceof ComponentFamily) {
				return ((ComponentFamily) value).getName();
		}
		if (value instanceof Component) {
			return ((Component) value).getName();
		}
		if (value instanceof ComponentVersion) {
			return ((ComponentVersion) value).getVersionNumber().toString();
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
