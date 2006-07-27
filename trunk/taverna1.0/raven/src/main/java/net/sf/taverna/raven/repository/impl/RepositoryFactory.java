package net.sf.taverna.raven.repository.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository.ArtifactClassLoader;

/**
 * DEPRECATED - do not use, flawed design logic :)
 * 
 * Use this class to create instances of the Repository. The primary function is to ensure
 * that all instances of Repository in client code have been loaded from the maven repository
 * via the ArtifactClassLoader, this avoids the potentially nasty situation where an
 * application is both loaded by and makes use of raven, in those cases there would otherwise
 * be the possibility that two different instances of the Repository object would exist
 * differing only in their underlying class loader.<p>
 * This class also ensures that multiple calls to acquire a repository with the same base
 * all return the same object, acting as a cache of Repository implementations.
 * @author Tom Oinn
 */
public class RepositoryFactory {

	private static Map<File,RepositoryFactory> factoryMap = new HashMap<File,RepositoryFactory>();
	
	public synchronized static RepositoryFactory getFactory(File base) {
		if (factoryMap.containsKey(base)) {
			return factoryMap.get(base);
		}
		factoryMap.put(base, new RepositoryFactory(base));
		return factoryMap.get(base);
	}
	
	private Repository theRepository = null;
	
	public RepositoryFactory(File base) {
		if (getClass().getClassLoader() instanceof ArtifactClassLoader) {
			theRepository = new LocalRepository(base);
			return;
		}
		else {
			// We're not in a raven based classloader so need to construct
			// one then use it to create the LocalRepository object through
			// reflection
			LocalRepository rep = new LocalRepository(base);
			try {
				ClassLoader cl = rep.getLoader(new BasicArtifact("taverna","raven","1.0"),null);
				try {
					Class repClass = cl.loadClass("net.sf.taverna.raven.repository.impl.LocalRepository");
				} catch (ClassNotFoundException e) {
					// Raven wasn't found in its own artifact, this is not a good thing
					System.out.println("Raven not found in raven artifact!");
					e.printStackTrace();
					theRepository = rep;
					return;
				}
			} catch (ArtifactNotFoundException e) {
				System.out.println("Can't find raven installed in this repository!");
				theRepository = rep;
				return;
			} catch (ArtifactStateException e) {
				System.out.println("Raven may be in an inconsistant state in the repository!");
				theRepository = rep;
				return;
			}
		}
	}
	
	public Repository getRepository() {
		return theRepository;
	}

}
