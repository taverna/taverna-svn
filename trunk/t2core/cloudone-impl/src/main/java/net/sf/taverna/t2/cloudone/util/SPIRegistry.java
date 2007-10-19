package net.sf.taverna.t2.cloudone.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Simple SPI lookup (using META-INF/services/interfaceName) to discover run time
 * class implementations. Extend in other classes to use the SPI lookup.
 * 
 * @author Stian Soiland
 * 
 * @param <SPI>
 *            The interface to discover
 */
public class SPIRegistry<SPI> {

	private Class<SPI> spi;
	private ArrayList<SPI> instances;
	private Set<String> classNames;

	/**
	 * Construct the SPI for the given interface.
	 * 
	 * @param spi Interface to discover
	 */
	public SPIRegistry(Class<SPI> spi) {
		this.spi = spi;
	}
	
	public void refresh() {
		instances = null;
		getInstances();
	}

	/**
	 * Return instantiated implementations of the SPI.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<SPI> getInstances() {
		if (instances != null) {
			return instances;
		}
		classNames = new HashSet<String>();
		instances = new ArrayList<SPI>();
		// TODO: Support Raven class loaders
		ClassLoader cl = getClass().getClassLoader();
		if (cl == null) {
			cl = ClassLoader.getSystemClassLoader();
		}
		Enumeration<URL> spiFiles;
		try {
			spiFiles = cl.getResources("META-INF/services/" + spi.getCanonicalName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return instances;
		}
		while (spiFiles.hasMoreElements()) {
			URL spiFile = spiFiles.nextElement();
			Scanner scanner;
			try {
				scanner = new Scanner(spiFile.openStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
				if (classNames.contains(name)) {
					System.out.println("Ignoring duplicate " + name);
					continue;
				}
//				System.out.println("SPI found class " + name);
				Class<? extends SPI> spiClass;
				try {
					spiClass = (Class<? extends SPI>) cl.loadClass(name);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				if (! (spi.isAssignableFrom(spiClass))) {
					System.err.println("Class " + spiClass + " did not implement " + spi);
					continue;
				}
				try {
					instances.add((SPI) spiClass.newInstance());
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassCastException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();	
				}
				classNames.add(name);
			}
		}
		return instances;
	}

}
