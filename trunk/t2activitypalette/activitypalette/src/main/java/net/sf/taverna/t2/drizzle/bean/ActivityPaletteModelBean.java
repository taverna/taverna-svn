/**
 * 
 */
package net.sf.taverna.t2.drizzle.bean;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
@XmlRootElement(namespace = "http://taverna.sf.net/t2/drizzle/bean/", name = "activityPaletteModel")
@XmlType(namespace = "http://taverna.sf.net/t2/drizzle/bean/", name = "activityPaletteModel")
public final class ActivityPaletteModelBean {

	private PropertiedObjectSetBean<ProcessorFactory> registryBean;

	/**
	 * @return the registryBean
	 */
	public synchronized final PropertiedObjectSetBean<ProcessorFactory> getRegistryBean() {
		return registryBean;
	}

	/**
	 * @param registryBean the registryBean to set
	 */
	public synchronized final void setRegistryBean(
			PropertiedObjectSetBean<ProcessorFactory> registryBean) {
		this.registryBean = registryBean;
	}

}
