package net.sf.taverna.raven.spi;

/**
 * Implement to be notified of changes to the list of concrete SPI instances
 * maintained by an InstanceRegistry
 * @author Tom Oinn
 */
public interface InstanceRegistryListener {

	public abstract void instanceRegistryUpdated(InstanceRegistry registry);
	
}
