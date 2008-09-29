package net.sf.taverna.platform.spring;

import java.util.List;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.spi.ArtifactFilter;
import net.sf.taverna.raven.spi.SpiRegistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean for the SpiRegistry functionality in Raven. Exposes the
 * repository, spi name and filter list properties. Initializes the spi registry
 * before returning it, constructed instances are ready to use.
 * 
 * @author Tom Oinn
 * 
 */
public class SpiRegistryFactoryBean implements FactoryBean {

	List<ArtifactFilter> filterList = null;
	Repository repository = null;
	String spiClassName = null;
	private Log log = LogFactory.getLog(SpiRegistryFactoryBean.class);

	public Object getObject() throws Exception {
		if (repository != null && spiClassName != null) {
			SpiRegistry registry = new SpiRegistry(repository, spiClassName,
					null);
			if (filterList != null) {
				registry.setFilters(filterList);
			}
			registry.updateRegistry();
			return registry;
		}
		log
				.error("Must specify repository and spi class name for spi registry");
		throw new RuntimeException();
	}

	public void setFilterList(List<ArtifactFilter> filterList) {
		this.filterList = filterList;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setSpiClassName(String spiClassName) {
		this.spiClassName = spiClassName;
	}

	/**
	 * @return SpiRegistry.class
	 */
	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return SpiRegistry.class;
	}

	/**
	 * Singleton by default
	 */
	public boolean isSingleton() {
		return true;
	}

}
