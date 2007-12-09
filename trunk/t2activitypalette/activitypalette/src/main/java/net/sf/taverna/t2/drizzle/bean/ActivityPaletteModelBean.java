/**
 * 
 */
package net.sf.taverna.t2.drizzle.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
@XmlRootElement(namespace = "http://taverna.sf.net/t2/drizzle/bean/", name = "activityPaletteModel")
@XmlType(namespace = "http://taverna.sf.net/t2/drizzle/bean/", name = "activityPaletteModel")
public final class ActivityPaletteModelBean {

	private List<SubsetKindConfigurationBean> subsetKindConfigurationBeans;
	
	private List<ProcessorFactoryAdapterBean> adapterBeans;

	/**
	 * @return the adapterBeans
	 */
	public synchronized final List<ProcessorFactoryAdapterBean> getAdapterBeans() {
		return adapterBeans;
	}

	/**
	 * @param adapterBeans the adapterBeans to set
	 */
	public synchronized final void setAdapterBeans(
			List<ProcessorFactoryAdapterBean> adapterBeans) {
		this.adapterBeans = adapterBeans;
	}

	/**
	 * @return the subsetKindConfigurationBeans
	 */
	public synchronized final List<SubsetKindConfigurationBean> getSubsetKindConfigurationBeans() {
		return subsetKindConfigurationBeans;
	}

	/**
	 * @param subsetKindConfigurationBeans the subsetKindConfigurationBeans to set
	 */
	public synchronized final void setSubsetKindConfigurationBeans(
			List<SubsetKindConfigurationBean> subsetKindConfigurationBeans) {
		this.subsetKindConfigurationBeans = subsetKindConfigurationBeans;
	}

}
