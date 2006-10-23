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

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.RepositoryListener;
import net.sf.taverna.raven.repository.impl.LocalRepository.ArtifactClassLoader;

/**
 * A typed registry of implementations of a particular
 * Service Provider Interface (SPI). Discovery of these
 * implementations is done using a similar mechanism
 * to that employed by the Apache project commons-discovery
 * that is to say a search for resources within known
 * jar archives for META-INF/services/<SPI name>
 * @author Tom Oinn
 */
public class SpiRegistry implements Iterable<Class> {
	
	private List<RegistryListener> listeners = new ArrayList<RegistryListener>();
	private Repository repository;
	private String classname;
	private Set<Artifact> newArtifacts = new HashSet<Artifact>();
	private List<ArtifactFilter> filters = new ArrayList<ArtifactFilter>();
	private ClassLoader parentLoader = null;
	private List<Class> implementations = null;
	private RepositoryListener rlistener;
	
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
		this.rlistener = new RepositoryListener() {
			// Listen to repository events and test any newly
			// ready artifacts for the appropriate SPI
			public void statusChanged(Artifact a, ArtifactStatus oldStatus, ArtifactStatus newStatus) {
				if (newStatus.equals(ArtifactStatus.Ready)) {
					//System.out.println(a+" "+oldStatus+"->"+newStatus);
					synchronized(SpiRegistry.this) {
						newArtifacts.add(a);
						updateRegistry();
					}
				}
			}
		};
		r.addRepositoryListener(rlistener);
		newArtifacts.addAll(r.getArtifacts(ArtifactStatus.Ready));		
	}
	
	@Override
	public void finalize() {
		repository.removeRepositoryListener(rlistener);
	}
	
	/**
	 * The class name for the registry to search over
	 */
	public String getClassName() {
		return this.classname;
	}
	
	/**
	 * Get the Class objects for all implementations of this SPI currently known
	 */
	public synchronized List<Class> getClasses() {
		if (implementations == null) {
			updateRegistry();
		}
		return this.implementations;
	}
	
	/**
	 * Add a new registry listener to be notified of any updates to
	 * this SpiRegistry
	 * @param l
	 */
	public void addRegistryListener(RegistryListener l) {
		synchronized (listeners) {
			if (listeners.contains(l) == false) {
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
		newArtifacts.clear();
		newArtifacts.addAll(repository.getArtifacts(ArtifactStatus.Ready));
		notifyListeners();
	}
	
	/**
	 * Clear all ArtifactFilter objects
	 */
	public synchronized void clearFilters() {
		implementations = null;
		filters.clear();
		newArtifacts.clear();
		newArtifacts.addAll(repository.getArtifacts(ArtifactStatus.Ready));
		notifyListeners();
	}
	
	/**
	 * Set the filter list
	 */
	public synchronized void setFilters(List<ArtifactFilter> newFilters) {
		implementations = null;
		filters = new ArrayList<ArtifactFilter>(newFilters);
		newArtifacts.clear();
		newArtifacts.addAll(repository.getArtifacts(ArtifactStatus.Ready));
		notifyListeners();
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
		// Do filtering
		
		// TODO - ensure that each classloader is only looked at once!
		Set<URL> alreadySeen = new HashSet<URL>();
		Set<Artifact> workingSet = new HashSet<Artifact>(newArtifacts);
		newArtifacts.clear();
		if (implementations == null) {
			implementations = new ArrayList<Class>();
		}
		boolean addedNew = false;
		for (ArtifactFilter af : filters) {
			workingSet = af.filter(workingSet); 												
		}
		for (Artifact a : workingSet) {
			try {				
				ClassLoader cl = repository.getLoader(a, parentLoader);
				Enumeration resources=cl.getResources("META-INF/services/"+classname);
				while (resources.hasMoreElements()) {
					URL resourceURL = (URL)resources.nextElement();
					if (resourceURL != null && alreadySeen.contains(resourceURL) == false) {
						// Found an appropriate SPI file
						alreadySeen.add(resourceURL);
						try {
							//System.out.println(" - found SPI file at "+resourceURL.toString());
							InputStream is = resourceURL.openStream();
							Scanner scanner = new Scanner(is);
							while (scanner.hasNext()) {
								String impName = scanner.next();
								Class impClass = cl.loadClass(impName);
								if (impClass.getClassLoader() instanceof ArtifactClassLoader || System.getProperty("raven.eclipse")!=null) {
									implementations.add(impClass);
									addedNew = true;
								}
							}
							scanner.close();
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
							// TODO - handle this exception, current behaviour to
							// ignore it isn't entirely unreasonable but we should
							// have some kind of logging here for debug porpoises.
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
							// TODO Auto-generated catch block - this should never
							// be reached but you never know, could be the author of
							// the plugin got the name wrong or depended on a class
							// that wasn't explicitly declared within the artifact
						}
					}
					else {
						//System.out.println(" - no SPI file for "+classname);
					}
				}
			} catch (ArtifactStateException ase) {
				ase.printStackTrace();
				// TODO - handle an attempt to fetch an incompatible artifact
			} catch (ArtifactNotFoundException anfe) {
				anfe.printStackTrace();
				// TODO - handle attempt to locate a classloader for an artifact
				// that isn't in the repository
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
			
		}
		if (addedNew) {
			notifyListeners();
		}
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
