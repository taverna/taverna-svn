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

import net.sf.taverna.t2.drizzle.decoder.CommonKey;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.drizzle.util.impl.PropertyKeySettingImpl;

/**
 * @author alanrw
 * 
 */
public final class SubsetKindConfiguration {
	private ArrayList<PropertyKey> keyList;
	private DefaultListModel treeListModel;
	private DefaultListModel treeTableListModel;
	private DefaultListModel tableListModel;
	private long lastChange = 0;

	/**
	 * @param keyProfile
	 */
	@SuppressWarnings("unchecked")
	public void initialiseKeyList(Set<PropertyKey> keyProfile) {
		this.keyList = new ArrayList<PropertyKey>(keyProfile);
		Collections.sort(this.keyList);
		this.treeListModel = new DefaultListModel();
		this.treeTableListModel = new DefaultListModel();
		this.tableListModel = new DefaultListModel();
		if (this.keyList.contains(CommonKey.CategoryKey)) {
			this.treeListModel.addElement(CommonKey.CategoryKey);
//			this.tableListModel.addElement(CommonKey.CategoryKey);
		}
		if (this.keyList.contains(CommonKey.BiomartMartKey)) {
			this.treeListModel.addElement(CommonKey.BiomartMartKey);
//			this.tableListModel.addElement(CommonKey.CategoryKey);
		}
		if (this.keyList.contains(CommonKey.MobyAuthorityKey)) {
			this.treeListModel.addElement(CommonKey.MobyAuthorityKey);
//			this.tableListModel.addElement(CommonKey.CategoryKey);
		}
		if (this.keyList.contains(CommonKey.NameKey)) {
			this.treeListModel.addElement(CommonKey.NameKey);
//			this.tableListModel.addElement(CommonKey.NameKey);
		}

		this.lastChange = System.currentTimeMillis();
	}

	/**
	 * @return the keyList
	 */
	public synchronized ArrayList<PropertyKey> getKeyList() {
		return this.keyList;
	}

	/**
	 * @return the treeListModel
	 */
	public synchronized DefaultListModel getTreeListModel() {
		return this.treeListModel;
	}

	/**
	 * @return the treeTableListModel
	 */
	public synchronized DefaultListModel getTreeTableListModel() {
		return this.treeTableListModel;
	}

	/**
	 * @return the tableListModel
	 */
	public synchronized DefaultListModel getTableListModel() {
		return this.tableListModel;
	}

	public List<PropertyKeySetting> getTreeKeySettings() {
		return getKeySettings(this.treeListModel);
	}

	public List<PropertyKeySetting> getTableKeySettings() {
		return getKeySettings(this.tableListModel);
	}

	private List<PropertyKeySetting> getKeySettings(DefaultListModel model) {
		List<PropertyKeySetting> result = new ArrayList<PropertyKeySetting>();

		for (Enumeration<?> e = model.elements(); e.hasMoreElements();) {
			PropertyKey key = (PropertyKey) e.nextElement();
			PropertyKeySetting setting = new PropertyKeySettingImpl();
			setting.setPropertyKey(key);
			result.add(setting);

		}
		return result;
	}

	/**
	 * @param keyList the keyList to set
	 */
	public synchronized final void setKeyList(ArrayList<PropertyKey> keyList) {
		this.lastChange = System.currentTimeMillis();
		this.keyList = keyList;
	}

	/**
	 * @return the lastChange
	 */
	public synchronized final long getLastChange() {
		return this.lastChange;
	}

	/**
	 * @param lastChange the lastChange to set
	 */
	public synchronized final void setLastChange(long lastChange) {
		this.lastChange = lastChange;
	}

	/**
	 * @param treeListModel the treeListModel to set
	 */
	public synchronized final void setTreeListModel(DefaultListModel treeListModel) {
		this.lastChange = System.currentTimeMillis();
		this.treeListModel = treeListModel;
	}

	/**
	 * @param treeTableListModel the treeTableListModel to set
	 */
	public synchronized final void setTreeTableListModel(
			DefaultListModel treeTableListModel) {
		this.lastChange = System.currentTimeMillis();
		this.treeTableListModel = treeTableListModel;
	}

	/**
	 * @param tableListModel the tableListModel to set
	 */
	public synchronized final void setTableListModel(DefaultListModel tableListModel) {
		this.lastChange = System.currentTimeMillis();
		this.tableListModel = tableListModel;
	}

}
