package net.sf.taverna.t2.platform.plugin;

import java.util.Set;

/**
 * A listener to handle change events from an SPIRegistry
 * 
 * @author Tom Oinn
 * 
 * @param <T>
 *            the type of the SPI registry
 */
public interface SPIRegistryListener<T> {

	/**
	 * Called when membership of the SPI registry is changed through addition or
	 * removal of SPI implementations.
	 * 
	 * @param entriesAdded
	 *            a set of new SPI registry entries added
	 * @param entriesRemoved
	 *            a set of previous SPI registry entries which are no longer
	 *            present
	 * @param currentMembership
	 *            the current membership of the SPI registry for convenience,
	 *            this is the membership after any changes have occured
	 */
	void spiMembershipChanged(Set<Class<T>> entriesAdded,
			Set<Class<T>> entriesRemoved, Set<Class<T>> currentMembership);

}
