/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.spi;

import java.util.List;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.log.Log4jLog;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.EclipseRepository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.spi.InstanceRegistry;
import net.sf.taverna.raven.spi.InstanceRegistryListener;
import net.sf.taverna.raven.spi.SpiRegistry;
import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.spi.SPIRegistry.SPIRegistryEvent;

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
public class SPIRegistry<SPI> implements Observable<SPIRegistryEvent> {

	public class RegistryListenerAdapter implements InstanceRegistryListener {

		@SuppressWarnings("unchecked")
		public void instanceRegistryUpdated(InstanceRegistry registry) {
			if (registry != instanceRegistry) {
				return;
			}
			multiCaster.notify(UPDATED);
		}

	}

	public static class SPIRegistryEvent {
	}

	/**
	 * Sent to {@link Observer}<{@link SPIRegistryEvent}>s when this SPI
	 * registry has been updated.
	 * 
	 */
	public static final SPIRegistryEvent UPDATED = new SPIRegistryEvent();

	static {
		// Set log4j logger for Raven
		Log4jLog log4jLog = new Log4jLog();
		Log.setImplementation(log4jLog);
	}

	private InstanceRegistry<SPI> instanceRegistry = null;
	private InstanceRegistryListener instanceRegistryListener = new RegistryListenerAdapter();
	private MultiCaster<SPIRegistryEvent> multiCaster = new MultiCaster<SPIRegistryEvent>(
			this);

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

	public void addObserver(Observer<SPIRegistryEvent> observer) {
		multiCaster.addObserver(observer);
	}

	private ClassLoader getClassLoader() {
		return getClass().getClassLoader();
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

	public List<Observer<SPIRegistryEvent>> getObservers() {
		return multiCaster.getObservers();
	}

	private synchronized InstanceRegistry<SPI> getRegistry() {
		if (instanceRegistry == null) {
			ravenSPIRegistry = new SpiRegistry(getRepository(), spi.getName(),
					getClassLoader());
			instanceRegistry = new InstanceRegistry<SPI>(ravenSPIRegistry,
					new Object[0]);
		}
		instanceRegistry.addRegistryListener(instanceRegistryListener);
		return instanceRegistry;
	}

	private Repository getRepository() {
		if (getClassLoader() instanceof LocalArtifactClassLoader) {
			return ((LocalArtifactClassLoader) getClassLoader())
					.getRepository();
		} else {
			return ApplicationRuntime.getInstance().getRavenRepository();
		}
	}

	/**
	 * Get the SPI instances found by this registry will be implementing.
	 * 
	 * @return The {@link Class} of the SPI interface
	 */
	public Class<SPI> getSpi() {
		return spi;
	}

	/**
	 * Reset the instanceRegistry causing it to be re-populated on the next call
	 * to getInstances.
	 */
	public void refresh() {
		if (instanceRegistry != null) {
			instanceRegistry.removeRegistryListener(instanceRegistryListener);
		}
		instanceRegistry = null;
		ravenSPIRegistry = null;
	}

	public void removeObserver(Observer<SPIRegistryEvent> observer) {
		multiCaster.removeObserver(observer);
	}

}
