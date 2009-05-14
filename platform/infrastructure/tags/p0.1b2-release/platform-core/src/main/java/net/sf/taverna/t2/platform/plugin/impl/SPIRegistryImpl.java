package net.sf.taverna.t2.platform.plugin.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import net.sf.taverna.t2.platform.plugin.PluginException;
import net.sf.taverna.t2.platform.plugin.SPIRegistry;
import net.sf.taverna.t2.platform.plugin.SPIRegistryListener;

/**
 * Implements SPIRegistry, do not construct this implementation directly,
 * instead use the getSPI method on PluginManager
 * 
 * @author Tom Oinn
 * 
 * @param <T>
 */
public class SPIRegistryImpl<T> implements SPIRegistry<T> {

	private Class<T> spiClass;

	private Set<Class<T>> implementations;

	private List<SPIRegistryListener<T>> listeners;

	@Override
	public String toString() {
		return "SPI registry for "+spiClass.getCanonicalName();
	}
	
	/**
	 * Construct a new SPI registry, initializing it with a set of classloaders
	 * to scan for implementations of the SPI on startup.
	 * 
	 * @param spiClass
	 * @param classLoaders
	 */
	SPIRegistryImpl(Class<T> spiClass, List<ClassLoader> classLoaders) {
		this.spiClass = spiClass;
		this.implementations = new HashSet<Class<T>>();
		this.listeners = new ArrayList<SPIRegistryListener<T>>();
		for (ClassLoader loader : classLoaders) {
			try {
				implementations.addAll(getImplementations(loader));
			} catch (PluginException pe) {
				//
			}
		}
	}

	public synchronized void addSPIRegistryListener(
			SPIRegistryListener<T> listener) {
		listeners.add(listener);
	}

	public void removeSPIRegistryListener(SPIRegistryListener<T> listener) {
		listeners.remove(listener);
	}

	public Iterator<Class<T>> iterator() {
		return new HashSet<Class<T>>(implementations).iterator();
	}

	@SuppressWarnings("unchecked")
	private Set<Class<T>> getImplementations(ClassLoader cl) {
		Set<Class<T>> result = new HashSet<Class<T>>();
		Enumeration<URL> resources;
		String resource = "META-INF/services/" + spiClass.getCanonicalName();
		try {
			resources = cl.getResources(resource);
		} catch (IOException ioe) {
			throw new PluginException("Unable to read resource from jar file",
					ioe);
		}
		//System.out.println("Resources for SPI "+spiClass.getName());
		while (resources.hasMoreElements()) {
			URL resourceURL = resources.nextElement();
			//System.out.println("  "+resourceURL);
			InputStream is;
			try {
				is = resourceURL.openStream();
			} catch (IOException ioe) {
				throw new PluginException("Unable to read services file", ioe);
			}
			Scanner scanner = new Scanner(is);
			scanner.useDelimiter("\n");
			while (scanner.hasNext()) {
				String impName = scanner.next().trim();
				//System.out.println("    "+impName);
				if (impName.length() == 0 || impName.startsWith("#")) {
					continue;
				}
				try {
					Class<T> impClass = (Class<T>) cl.loadClass(impName);
					//System.out.println("      "+impClass.getCanonicalName());
					result.add(impClass);
				} catch (ClassNotFoundException cnfe) {
					//cnfe.printStackTrace();
					throw new PluginException(
							"Unable to resolve plugin implementation class",
							cnfe);
				}
			}
			scanner.close();
			try {
				is.close();
			} catch (IOException e) {
				// Ignore this for now
				continue;
			}
		}
		return result;
	}

	synchronized void classLoaderAdded(ClassLoader cl) {
		Set<Class<T>> newImplementations = getImplementations(cl);
		if (!newImplementations.isEmpty()) {
			implementations.addAll(newImplementations);
			for (SPIRegistryListener<T> listener : listeners) {
				listener.spiMembershipChanged(newImplementations, Collections
						.<Class<T>> emptySet(), implementations);
			}
		}
	}

	synchronized void classLoaderRemoved(ClassLoader cl) {
		Set<Class<T>> implementationsRemoved = getImplementations(cl);
		if (!implementationsRemoved.isEmpty()) {
			implementations.removeAll(implementationsRemoved);
			for (SPIRegistryListener<T> listener : listeners) {
				listener.spiMembershipChanged(
						Collections.<Class<T>> emptySet(),
						implementationsRemoved, implementations);
			}
		}
	}

}
