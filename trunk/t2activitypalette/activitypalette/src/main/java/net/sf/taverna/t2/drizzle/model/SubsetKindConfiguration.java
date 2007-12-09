/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import net.sf.taverna.t2.drizzle.util.ObjectFactory;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;

/**
 * @author alanrw
 * 
 */
public final class SubsetKindConfiguration {
	private ArrayList<PropertyKey> keyList;
	private DefaultListModel treeListModel;
	private DefaultListModel treeTableListModel;
	private DefaultListModel tableListModel;

	public void initialiseKeyList(Set<PropertyKey> keyProfile) {
		keyList = new ArrayList(keyProfile);
		Collections.sort(keyList);
		treeListModel = new DefaultListModel();
		treeTableListModel = new DefaultListModel();
		tableListModel = new DefaultListModel();
		for (PropertyKey pk : keyList) {
			treeListModel.addElement(pk);
			tableListModel.addElement(pk);
		}
	}

	/**
	 * @return the keyList
	 */
	public synchronized ArrayList<PropertyKey> getKeyList() {
		return keyList;
	}

	/**
	 * @return the treeListModel
	 */
	public synchronized DefaultListModel getTreeListModel() {
		return treeListModel;
	}

	/**
	 * @return the treeTableListModel
	 */
	public synchronized DefaultListModel getTreeTableListModel() {
		return treeTableListModel;
	}

	/**
	 * @return the tableListModel
	 */
	public synchronized DefaultListModel getTableListModel() {
		return tableListModel;
	}

	public List<PropertyKeySetting> getTreeKeySettings() {
		return getKeySettings(treeListModel);
	}

	public List<PropertyKeySetting> getTableKeySettings() {
		return getKeySettings(tableListModel);
	}

	private List<PropertyKeySetting> getKeySettings(DefaultListModel model) {
		List<PropertyKeySetting> result = new ArrayList<PropertyKeySetting>();

		for (Enumeration e = model.elements(); e.hasMoreElements();) {
			PropertyKey key = (PropertyKey) e.nextElement();
			PropertyKeySetting setting = ObjectFactory
					.getInstance(PropertyKeySetting.class);
			setting.setPropertyKey(key);
			result.add(setting);

		}
		return result;
	}

	/**
	 * @param keyList the keyList to set
	 */
	public synchronized final void setKeyList(ArrayList<PropertyKey> keyList) {
		this.keyList = keyList;
	}

	/**
	 * @param treeListModel the treeListModel to set
	 */
	public synchronized final void setTreeListModel(DefaultListModel treeListModel) {
		this.treeListModel = treeListModel;
	}

	/**
	 * @param treeTableListModel the treeTableListModel to set
	 */
	public synchronized final void setTreeTableListModel(
			DefaultListModel treeTableListModel) {
		this.treeTableListModel = treeTableListModel;
	}

	/**
	 * @param tableListModel the tableListModel to set
	 */
	public synchronized final void setTableListModel(DefaultListModel tableListModel) {
		this.tableListModel = tableListModel;
	}

}
