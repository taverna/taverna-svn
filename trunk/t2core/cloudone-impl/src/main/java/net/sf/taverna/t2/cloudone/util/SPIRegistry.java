package net.sf.taverna.t2.cloudone.util;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Simple SPI lookup (using META-INF/services/interfaceName) to discover run
 * time class implementations. Extend in other classes to use the SPI lookup.
 * 
 * @author Stian Soiland
 * 
 * @param <SPI>
 *            The interface to discover
 */
public class SPIRegistry<SPI> {

	private static Logger logger = Logger.getLogger(SPIRegistry.class);
	private Class<SPI> spi;
	private List<SPI> instances;
	private Set<String> classNames;
	private Map<String, Class<? extends SPI>> classes;

	/**
	 * Construct the SPI for the given interface.
	 * 
	 * @param spi
	 *            Interface to discover
	 */
	public SPIRegistry(Class<SPI> spi) {
		this.spi = spi;
	}

	public void refresh() {
		instances = null;
		getInstances();
	}

	public List<SPI> getInstances() {
		synchronized (this) {
			if (instances != null) {
				return instances;
			}
		}
		List<SPI> foundInstances = new ArrayList<SPI>();
		for (Class<? extends SPI> spiClass : getClasses().values()) {
			try {
				foundInstances.add((SPI) spiClass.newInstance());
			} catch (InstantiationException e) {
				logger.warn("Could not instantiate " + spiClass, e);
			} catch (IllegalAccessException e) {
				logger.warn("Could not access " + spiClass, e);
			} catch (ClassCastException e) {
				logger.warn("Could not cast " + spiClass + " to " + spi, e);
			}
		}
		synchronized (this) {
			instances = foundInstances;
		}
		return foundInstances;
	}

	/**
	 * Return instantiated implementations of the SPI.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Collection<String> getClassNames() {
		synchronized (this) {
			if (classNames != null) {
				return classNames;
			}
		}
		Set<String> foundClassNames = new HashSet<String>();
		// TODO: Support Raven class loaders
		ClassLoader cl = getClassLoader();
		Enumeration<URL> spiFiles;
		String spiResource = "META-INF/services/"
			+ spi.getCanonicalName();
		try {
			spiFiles = cl.getResources(spiResource);
		} catch (IOException e) {
			logger.warn("Could not find resource " + spiResource, e);
			return foundClassNames; // empty
		}
		while (spiFiles.hasMoreElements()) {
			URL spiFile = spiFiles.nextElement();
			Scanner scanner;
			try {
				scanner = new Scanner(spiFile.openStream());
			} catch (IOException e) {
				logger.warn("Could not read " + spiFile, e);
				continue;
			}
			while (scanner.hasNextLine()) {
				// Remove comments
				String line = scanner.nextLine().replaceAll("\\s*#.*", "");
				// Remove leading and trailing white-space
				line = line.replaceFirst("^\\s*", "");
				String name = line.replaceFirst("\\s*$", "");
				if (name.equals("")) {
					continue; // just blank line or comment
				}
				if (foundClassNames.contains(name)) {
					logger.warn("Ignoring duplicate SPI " + name);
					continue;
				}
				foundClassNames.add(name);
			}
		}
		synchronized (this) {
			classNames = foundClassNames;
		}
		return foundClassNames;
	}

	private ClassLoader getClassLoader() {
		ClassLoader cl = getClass().getClassLoader();
		if (cl == null) {
			cl = ClassLoader.getSystemClassLoader();
		}
		return cl;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Class<? extends SPI>> getClasses() {
		synchronized (this) {
			if (classes != null) {
				return classes;
			}
		}
		Map<String, Class<? extends SPI>> foundClasses = new HashMap<String, Class<? extends SPI>>();
		ClassLoader cl = getClassLoader();
		for (String className : getClassNames()) {
			Class<? extends SPI> spiClass;
			try {
				spiClass = (Class<? extends SPI>) cl.loadClass(className);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			if (!(spi.isAssignableFrom(spiClass))) {
				System.err.println("Class " + spiClass + " did not implement "
						+ spi);
				continue;
			}
			if (spiClass.isInterface()) {
				System.err.println(spiClass + " is an interface, not a class");
				continue;
			}
			if (Modifier.isAbstract(spiClass.getModifiers())) {
				System.err.println(spiClass + " is an abstract class");
				continue;
			}
			foundClasses.put(className, spiClass);
		}
		synchronized (this) {
			classes = foundClasses;
		}
		return foundClasses;
	}

}
