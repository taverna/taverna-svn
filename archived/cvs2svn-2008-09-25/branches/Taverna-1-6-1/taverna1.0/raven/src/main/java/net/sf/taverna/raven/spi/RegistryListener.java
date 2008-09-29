package net.sf.taverna.raven.spi;

/**
 * Implement to listen for cases where an SpiRegistry object is changing,
 * typically due to the runtime deployment of new plugin versions via
 * the Raven repository manager.
 * @author Tom Oinn
 *
 */
public interface RegistryListener {

	public abstract void spiRegistryUpdated(SpiRegistry registry);
	
}
