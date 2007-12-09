/**
 * 
 */
package net.sf.taverna.t2.drizzle.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.StringKey;

/**
 * @author alanrw
 *
 */
@XmlRootElement(namespace = "http://taverna.sf.net/t2/drizzle/bean/", name = "subsetKindConfiguration")
@XmlType(namespace = "http://taverna.sf.net/t2/drizzle/bean/", name = "subsetKindConfiguration")
public final class SubsetKindConfigurationBean {
	
	private String kind;
	
	private List<String> keyList;
	
	private List<String> treeKeyList;
	
	private List<String> treeTableKeyList;
	
	private List<String> tableKeyList;

	/**
	 * @return the kind
	 */
	public synchronized final String getKind() {
		return kind;
	}

	/**
	 * @param kind the kind to set
	 */
	public synchronized final void setKind(String kind) {
		this.kind = kind;
	}

	/**
	 * @return the keyList
	 */
	public synchronized final List<String> getKeyList() {
		return keyList;
	}

	/**
	 * @param keyList the keyList to set
	 */
	public synchronized final void setKeyList(List<String> keyList) {
		this.keyList = keyList;
	}

	/**
	 * @return the treeKeyList
	 */
	@XmlElement(name="treeKey")
	public synchronized final List<String> getTreeKeyList() {
		return treeKeyList;
	}

	/**
	 * @param treeKeyList the treeKeyList to set
	 */
	public synchronized final void setTreeKeyList(List<String> treeKeyList) {
		this.treeKeyList = treeKeyList;
	}

	/**
	 * @return the treeTableKeyList
	 */
	@XmlElement(name="treeTableKey")
	public synchronized final List<String> getTreeTableKeyList() {
		return treeTableKeyList;
	}

	/**
	 * @param treeTableKeyList the treeTableKeyList to set
	 */
	public synchronized final void setTreeTableKeyList(
			List<String> treeTableKeyList) {
		this.treeTableKeyList = treeTableKeyList;
	}

	/**
	 * @return the tableKeyList
	 */
	@XmlElement(name="tableKey")
	public synchronized final List<String> getTableKeyList() {
		return tableKeyList;
	}

	/**
	 * @param tableKeyList the tableKeyList to set
	 */
	public synchronized final void setTableKeyList(List<String> tableKeyList) {
		this.tableKeyList = tableKeyList;
	}

}
