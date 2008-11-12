package net.sf.taverna.t2.platform.plugin;

/**
 * An SPI registry tracks and manages implementations of a Service Provider
 * Interface or SPI.
 * 
 * @param <T>
 *            the interface type of this SPI registry, all members will extend
 *            this type.
 * 
 * @author Tom Oinn
 * 
 */
public interface SPIRegistry<T> extends Iterable<Class<T>> {

	/**
	 * Add a new SPIRegistryListener to this SPIRegistry to be notified of
	 * membership change events
	 */
	void addSPIRegistryListener(SPIRegistryListener<T> listener);

	/**
	 * Remove a previously registered SPIRegistryListener
	 */
	void removeSPIRegistryListener(SPIRegistryListener<T> listener);

}
