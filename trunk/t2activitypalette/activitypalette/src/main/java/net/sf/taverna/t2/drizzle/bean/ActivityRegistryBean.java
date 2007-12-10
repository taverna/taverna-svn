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
@XmlRootElement(namespace = "http://taverna.sf.net/t2/drizzle/bean/", name = "activityRegistry")
@XmlType(namespace = "http://taverna.sf.net/t2/drizzle/bean/", name = "activityRegistry")
public final class ActivityRegistryBean {

	private List<ProcessorFactoryAdapterBean> adapterBeans;

	/**
	 * @return the adapterBeans
	 */
	public synchronized final List<ProcessorFactoryAdapterBean> getAdapterBeans() {
		return this.adapterBeans;
	}

	/**
	 * @param adapterBeans the adapterBeans to set
	 */
	public synchronized final void setAdapterBeans(
			List<ProcessorFactoryAdapterBean> adapterBeans) {
		this.adapterBeans = adapterBeans;
	}

}
