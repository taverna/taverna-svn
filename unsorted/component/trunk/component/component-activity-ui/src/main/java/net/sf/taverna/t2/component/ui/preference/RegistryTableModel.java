/**
 * 
 */
package net.sf.taverna.t2.component.ui.preference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.table.DefaultTableModel;

import net.sf.taverna.t2.component.registry.ComponentRegistry;

/**
 * @author alanrw
 *
 */
public class RegistryTableModel extends DefaultTableModel {
	
	private SortedMap<String, ComponentRegistry> registryMap = new TreeMap<String, ComponentRegistry>();
	
	public RegistryTableModel() {
		super(new String[] {"Registry name", "Registry location"}, 0);
	}

	public void setRegistryMap(SortedMap<String, ComponentRegistry> registries) {
		registryMap.clear();
		for (String s : registries.keySet()) {
			registryMap.put(s, registries.get(s));
		}
		updateRows();
	}
	
	public void updateRows() {
		super.setRowCount(0);
		for (String key : registryMap.keySet()) {
			super.addRow(new Object[] {key, registryMap.get(key).getRegistryBase().toExternalForm()});
		}		
	}
	
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public void removeRow(int row) {
		String key = (String) super.getValueAt(row, 0);
		registryMap.remove(key);
		super.removeRow(row);
	}
	
	public void insertRegistry(String name, ComponentRegistry newRegistry) {
		registryMap.put(name, newRegistry);
		updateRows();
	}

	/**
	 * @return the registryMap
	 */
	public SortedMap<String, ComponentRegistry> getRegistryMap() {
		return registryMap;
	}

}
