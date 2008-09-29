/**
 * 
 */
package net.sf.taverna.t2.drizzle.bean;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author alanrw
 *
 */
@XmlRootElement(namespace = "http://taverna.sf.net/t2/drizzle/bean/", name = "processorFactoryAdapter")
@XmlType(namespace = "http://taverna.sf.net/t2/drizzle/bean/", name = "processorFactoryAdapter")
public final class ProcessorFactoryAdapterBean {
	
	private byte[] xmlFragment;

	/**
	 * @return the xmlFragment
	 */
	public synchronized final byte[] getXmlFragment() {
		return this.xmlFragment;
	}

	/**
	 * @param xmlFragment the xmlFragment to set
	 */
	public synchronized final void setXmlFragment(byte[] xmlFragment) {
		this.xmlFragment = xmlFragment;
	}

}
