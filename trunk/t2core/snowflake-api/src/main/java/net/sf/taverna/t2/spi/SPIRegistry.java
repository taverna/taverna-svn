package net.sf.taverna.t2.spi;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.log.Log4jLog;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.EclipseRepository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.spi.InstanceRegistry;
import net.sf.taverna.raven.spi.SpiRegistry;

/**
 * Simple SPI lookup (using META-INF/services/interfaceName) to discover
 * run-time class implementations. Subclass or instantiate with given interface
 * to use the SPI lookup.
 * 
 * This is based upon the {@link InstanceRegistry} and {@link SpiRegistry} in
 * Raven.
 * 
 * @author Stuart Owen
 * @author Stian Soiland
 * 
 * @param <SPI>
 *            The interface type that the SPI classes implement
 */
public class SPIRegistry<SPI> {

	static {
		// Set log4j logger for Raven
		Log4jLog log4jLog = new Log4jLog();
		Log.setImplementation(log4jLog);
	}

	private InstanceRegistry<SPI> instanceRegistry = null;
	private SpiRegistry ravenSPIRegistry = null;
	private final Class<SPI> spi;

	/**
	 * Construct the SPI for the given interface.
	 * 
	 * @param spi
	 *            Interface to discover
	 */
	public SPIRegistry(Class<SPI> spi) {
		this.spi = spi;
	}

	/**
	 * Reset the instanceRegistry causing it to be re-populated on the next call
	 * to getInstances.
	 */
	public void refresh() {
		this.instanceRegistry = null;
		this.ravenSPIRegistry = null;
	}

	/**
	 * Get all discovered instances.
	 * 
	 * @return a {@link List} of instances of the classes discovered by the
	 *         registry
	 */
	public List<SPI> getInstances() {
		return getRegistry().getInstances();
	}

	private synchronized InstanceRegistry<SPI> getRegistry() {
		if (instanceRegistry == null) {
			ravenSPIRegistry = new SpiRegistry(getRepository(), spi.getName(),
					getClassLoader());
			instanceRegistry = new InstanceRegistry<SPI>(ravenSPIRegistry,
					new Object[0]);
		}
		return instanceRegistry;
	}

	private Repository getRepository() {
		if (getClassLoader() instanceof LocalArtifactClassLoader) {
			return ((LocalArtifactClassLoader) getClassLoader())
					.getRepository();
		} else {
			// Do the ugly hack in one place
			System.setProperty("raven.eclipse", "true");
			return new EclipseRepository();
		}
	}

	private ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}

	/**
	 * @return a {@link Map} of discovered implementing classes, the key being
	 *         the full classname and the value being the discovered {@link Class}?
	 *         extends SPI> itself.
	 * @deprecated This method will be removed once the
	 *             {@link net.sf.taverna.t2.cloudone.bean.BeanableRegistry} has
	 *             have been refactored to use factory classes (TAV-662).
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public Map<String, Class<? extends SPI>> getClasses() {
		getRegistry();
		List<Class> classes = ravenSPIRegistry.getClasses();
		System.out.println("Found for " + spi + ": " + classes);
		Map<String, Class<? extends SPI>> result = new HashMap<String, Class<? extends SPI>>();
		for (Class c : classes) {
			result.put(c.getName(), c);
		}
		return result;
	}

	/**
	 * Get the SPI instances found by this registry will be implementing.
	 * 
	 * @return The {@link Class} of the SPI interface
	 */
	public Class<SPI> getSpi() {
		return spi;
	}

}
