package net.sf.taverna.platform.spring.orm.hibernate3;

import java.util.List;

import net.sf.taverna.raven.spi.SpiRegistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.util.ReflectHelper;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * An extension to the regular spring hibernate session factory which allows an
 * additional property containing a list of SpiRegistry beans. If this is
 * defined then each SpiRegistry in the list is used to get a list of Class
 * objects and these are in turn used to add classes to the hibernate
 * configuration.
 * 
 * @author Tom Oinn
 */
public class SpiRegistryAwareLocalSessionFactoryBean extends
		LocalSessionFactoryBean {

	private Log log = LogFactory
			.getLog(SpiRegistryAwareLocalSessionFactoryBean.class);
	private List<SpiRegistry> spiRegistries = null;

	/**
	 * Set the list of SPI registries from which to pull class definitions for
	 * mapped beans.
	 * 
	 * @param spis
	 */
	public void setSpiRegistries(List<SpiRegistry> spis) {
		log.debug("Set SPI registry list");
		this.spiRegistries = spis;
	}

	/**
	 * Use any defined spiRegistries, adding any classes they contain to the
	 * configuration before returning it. Initially delegates to the superclass,
	 * so explicitly defined mappings will be mapped before those from the
	 * SpiRegistry(s)
	 */
	@Override
	protected Configuration newConfiguration() {
		Configuration conf = super.newConfiguration();
		log.trace("Starting SPI registry based configuration");
		if (spiRegistries != null) {
			for (SpiRegistry spi : spiRegistries) {
				for (Class<?> theClass : spi.getClasses()) {
					log.info("Added class '" + theClass.getCanonicalName()
							+ "' from SPI to hibernate configuration");
					ReflectHelper.registerClass(theClass);
					conf.addClass(theClass);
				}
			}
		} else {
			log.info("No SPI registries defined");
		}
		log.trace("Done SPI registry based configuration");
		return conf;
	}

}
