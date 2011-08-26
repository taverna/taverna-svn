package net.sf.taverna.raven.spi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import net.sf.taverna.raven.RavenException;
import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.RepositoryListener;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;

/**
 * A typed registry of implementations of a particular
 * Service Provider Interface (SPI). Discovery of these
 * implementations is done using a similar mechanism
 * to that employed by the Apache project commons-discovery
 * that is to say a search for resources within known
 * jar archives for META-INF/services/<SPI name>
 * @author Tom Oinn
 */
public class SpiRegistry implements Iterable<Class>, ArtifactFilterListener {
	
	private static Log logger = Log.getLogger(SpiRegistry.class);
	
	private List<RegistryListener> listeners = new ArrayList<RegistryListener>();
	private Repository repository;
	private String classname;
	private Set<Artifact> newArtifacts = new HashSet<Artifact>();
	private List<ArtifactFilter> filters = new ArrayList<ArtifactFilter>();
	private ClassLoader parentLoader = null;
	private List<Class> implementations = null;
	private RepositoryListener rlistener;
	private List<Class> previousImplementations;
	
	/**
	 * Create a new SpiRegistry based on a particular repository
	 * and searching for the specified SPI classname. Note that
	 * no scan for implementations is done at this point to allow
	 * interested parties to register SpiListener instances first.
	 * @param r the Repository which will be used to locate
	 * implementations of the SPI
	 * @param classname of the SPI to search for
	 * @param parentLoader a ClassLoader to use as the parent for
	 * any entries loaded here, or null for no parent (most likely
	 * behaviour is to leave as null)
	 */
	public SpiRegistry(Repository r, String classname, ClassLoader parentLoader) {		
		this.repository = r;
		this.classname = classname;
		this.parentLoader = parentLoader;
		this.rlistener = new AddNewArtifactsListener(this);
		r.addRepositoryListener(rlistener);
		newArtifacts.addAll(r.getArtifacts(ArtifactStatus.Ready));		
	}
	
	public void addNewArtifact(Artifact a) {
		newArtifacts.add(a);
	}
	
	@Override
	public void finalize() {
		// FIXME: Will this ever work? rlistener references repository
		// and we reference rlistener...
		repository.removeRepositoryListener(rlistener);
	}
	
	/**
	 * The class name for the registry to search over
	 */
	public String getClassName() {
		return classname;
	}
	
	/**
	 * Get the Class objects for all implementations of this SPI currently known
	 */
	public synchronized List<Class> getClasses() {
		if (implementations == null) {
			updateRegistry();
		}
		return implementations;
	}
	
	/**
	 * Add a new registry listener to be notified of any updates to
	 * this SpiRegistry
	 * @param l
	 */
	public void addRegistryListener(RegistryListener l) {
		synchronized (listeners) {
			if (! listeners.contains(l)) {
				listeners.add(l);
			}
		}
	}
	
	/**
	 * Remove a listener from this SpiRegistry
	 * @param l
	 */
	public void removeRegistryListener(RegistryListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
	}
	
	/**
	 * Add a new ArtifactFilter
	 */
	public synchronized void addFilter(ArtifactFilter af) {		
		implementations = null;
		filters.add(af);
		af.addArtifactFilterListener(this);
		newArtifacts.clear();
		newArtifacts.addAll(repository.getArtifacts(ArtifactStatus.Ready));
		updateRegistry();
	}
	
	/**
	 * Clear all ArtifactFilter objects
	 */
	public synchronized void clearFilters() {		
		implementations = null;
		for (ArtifactFilter filter : filters) {
			filter.removeArtifactFilterListener(this);
		}
		filters.clear();
		newArtifacts.clear();
		newArtifacts.addAll(repository.getArtifacts(ArtifactStatus.Ready));
		updateRegistry();
	}
	
	/**
	 * Set the filter list
	 */
	public synchronized void setFilters(List<ArtifactFilter> newFilters) {		
		implementations = null;
		for (ArtifactFilter filter : filters) {
			filter.removeArtifactFilterListener(this);
		}
		filters = new ArrayList<ArtifactFilter>(newFilters);
		for (ArtifactFilter filter : filters) {
			filter.addArtifactFilterListener(this);
		}
		newArtifacts.clear();
		newArtifacts.addAll(repository.getArtifacts(ArtifactStatus.Ready));		
		updateRegistry();
	}
	
	public void filterChanged(ArtifactFilter filter) {			
		implementations = null;
		newArtifacts.clear();
		newArtifacts.addAll(repository.getArtifacts(ArtifactStatus.Ready));		
		updateRegistry();
	}
	
	/**
	 * Apply all filters to the set of new artifacts in turn, then
	 * for each Artifact which passes all the filters see if it contains
	 * a service entry for the SPI we're meant to be looking after. If
	 * it does then parse the entry and extract the list of implementation
	 * classnames. Class objects corresponding to these are then added to
	 * the set of current known implementations.
	 */
	public synchronized void updateRegistry() {
						
		Set<URL> seenURLs = new HashSet<URL>();
		Set<ClassLoader> seenClassLoaders = new HashSet<ClassLoader>();
		Set<Artifact> workingSet = new HashSet<Artifact>(newArtifacts);
		newArtifacts.clear();
		if (implementations == null) {
			implementations = new ArrayList<Class>();
		}
		boolean addedNew = false;
		
//		 Do filtering
		for (ArtifactFilter af : filters) {			
			workingSet = af.filter(workingSet); 											
		}
		for (Artifact a : workingSet) {			
			ClassLoader cl;
			try {
				cl = repository.getLoader(a, parentLoader);
			} catch (RavenException e) {
				logger.error("Could not get class loader for " + a, e);
				continue;
			}
			if (seenClassLoaders.contains(cl)) {
				continue;
			}
			seenClassLoaders.add(cl);
			Enumeration<URL> resources;
			String resource = "META-INF/services/"+classname;
			try {
				resources = cl.getResources(resource);
			} catch (IOException e) {
				logger.warn("Could not find resource " + resource, e);
				continue;
			}
			while (resources.hasMoreElements()) {
				URL resourceURL = resources.nextElement();				
				if (resourceURL == null || seenURLs.contains(resourceURL)) {
					logger.debug("No SPI file for "+classname);
					continue;
				}
				// Found an appropriate SPI file
				seenURLs.add(resourceURL);
				logger.debug("Found SPI file at "+resourceURL);
				InputStream is;
				try {
					is = resourceURL.openStream();
				} catch (IOException ex) {
					logger.warn("Could not read " + resourceURL, ex);
					continue;
				}
				Scanner scanner = new Scanner(is);
				scanner.useDelimiter("\n");
				while (scanner.hasNext()) {
					String impName = scanner.next().trim();						
					if (impName.length()==0 || impName.startsWith("#")) { //ignore commented entries or blank lines
						//logger.debug("Skipping line " + impName);
						continue;
					} else {
						logger.info("Loading SPI " + impName);
					}
					Class impClass;
					try {
						impClass = cl.loadClass(impName);
					} catch (ClassNotFoundException e) {
						logger.warn("Could not find class " + impName + " using " + cl, e);
						continue;
					}
					if (impClass.getClassLoader() instanceof LocalArtifactClassLoader || System.getProperty("raven.eclipse")!=null) {						
						implementations.add(impClass);	
						//only mark as new if this class did not appear in the previous set of implementations, i.e.
						//is actually new.						
						if (previousImplementations==null || !previousImplementations.contains(impClass)) {							
							addedNew = true;
						}
					}
				}
				scanner.close();
				try {
					is.close();
				} catch (IOException e) {
					logger.warn("Could not close stream " + resourceURL, e);
					continue;
				}
			}
		}
		boolean removedOld=implementationRemoved();
		previousImplementations=implementations;		
		
		if (addedNew || removedOld) {			
			notifyListeners();
		}				
	}
	
	/**
	 * Returns true if any classes stored in previousImplementations have now been removed.
	 * @return
	 */
	private boolean implementationRemoved() {
		if (previousImplementations==null) return false;
		for (Class c : previousImplementations) {
			if (!implementations.contains(c)) return true;
		}
		return false;
	}
	
	private void notifyListeners() {
		synchronized(listeners) {
			for (RegistryListener rl : listeners) {
				rl.spiRegistryUpdated(this);
			}
		}
	}
	
	public Iterator<Class> iterator() {
		return getClasses().iterator();
	}
	
}
class AddNewArtifactsListener implements RepositoryListener {

	private static Log logger = Log.getLogger(AddNewArtifactsListener.class);
	private SpiRegistry registry;

	AddNewArtifactsListener(SpiRegistry registry) {
		this.registry = registry;
	}

	// Listen to repository events and test any newly
	// ready artifacts for the appropriate SPI
	public void statusChanged(Artifact a, ArtifactStatus oldStatus, ArtifactStatus newStatus) {
		if (newStatus.equals(ArtifactStatus.Ready)) {
			logger.debug(a+" "+oldStatus+"->"+newStatus);
			synchronized(registry) {
				registry.addNewArtifact(a);				
				registry.updateRegistry();
			}
		}
	}
}
