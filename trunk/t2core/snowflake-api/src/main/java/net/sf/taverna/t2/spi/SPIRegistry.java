package net.sf.taverna.t2.spi;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.EclipseRepository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.spi.InstanceRegistry;
import net.sf.taverna.raven.spi.SpiRegistry;
import net.sf.taverna.t2.cloudone.util.BeanableRegistry;

/**
 * Simple SPI lookup (using META-INF/services/interfaceName) to discover run
 * time class implementations. Extend in other classes to use the SPI lookup.
 * 
 * This is based upon the InstanceRegistry and SpiRegistry contained in Raven.
 * 
 * @author Stuart Owen
 * @author Stian Soiland
 * 
 * @param <SPI>
 *            The interface type that the SPI classes implement
 */
public class SPIRegistry<SPI> {

	private InstanceRegistry<SPI> instanceRegistry;
	private SpiRegistry ravenSPIRegistry;
	Class<SPI> spi;

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
	 * Resets the instanceRegistry causing it to be re-populated on the
	 * next call to getInstances.
	 */
	public void refresh() {
		this.instanceRegistry = null;
		this.ravenSPIRegistry = null;
		getInstances();
	}

	/**
	 * @return a List of instances of the classes discovered by the registry
	 */
	public List<SPI> getInstances() {
		return getRegistry().getInstances();
	}

	/**
	 * Returns instantiated implementations of the SPI.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Collection<String> getClassNames() {
		return getClasses().keySet();
	}

	private InstanceRegistry<SPI> getRegistry() {
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
			return new EclipseRepository();
		}
	}

	private ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}

	/**
	 * @return a Map of classes, the key being the classname and the value being the Class<? extends SPI> itself.
	 * @deprecated this method will be removed once the {@link BeanableRegistry} has have been refactored to use factory classes.
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public Map<String, Class<? extends SPI>> getClasses() {
		getRegistry();
		List<Class> classes = ravenSPIRegistry.getClasses();
		Map<String, Class<? extends SPI>> result = new HashMap<String, Class<? extends SPI>>();
		for (Class c : classes) {
			result.put(c.getName(), c);
		}
		return result;
	}

}
