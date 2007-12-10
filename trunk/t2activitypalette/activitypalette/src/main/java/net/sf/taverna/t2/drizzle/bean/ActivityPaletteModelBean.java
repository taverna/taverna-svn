/**
 * 
 */
package net.sf.taverna.t2.drizzle.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author alanrw
 *
 */
@XmlRootElement(namespace = "http://taverna.sf.net/t2/drizzle/bean/", name = "activityPaletteModel")
@XmlType(namespace = "http://taverna.sf.net/t2/drizzle/bean/", name = "activityPaletteModel")
public final class ActivityPaletteModelBean {

	private List<SubsetKindConfigurationBean> subsetKindConfigurationBeans;


	/**
	 * @return the subsetKindConfigurationBeans
	 */
	public synchronized final List<SubsetKindConfigurationBean> getSubsetKindConfigurationBeans() {
		return this.subsetKindConfigurationBeans;
	}

	/**
	 * @param subsetKindConfigurationBeans the subsetKindConfigurationBeans to set
	 */
	public synchronized final void setSubsetKindConfigurationBeans(
			List<SubsetKindConfigurationBean> subsetKindConfigurationBeans) {
		this.subsetKindConfigurationBeans = subsetKindConfigurationBeans;
	}

}
