package net.sf.taverna.t2.platform.spring;

import java.net.URL;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import net.sf.taverna.t2.platform.raven.Raven;
import net.sf.taverna.t2.platform.raven.RavenException;

/**
 * Simple class that delegates to a provided instance of Raven for all Raven
 * interfaces, and in addition allows for the provision of a default set of
 * maven 2 repository locations on construction. Used by the repository factory
 * bean.
 * 
 * @author Tom Oinn
 * 
 */
public class InternalRaven implements Raven {

	private Raven delegate;

	private List<URL> repositories;

	InternalRaven(Raven delegate, List<URL> repositories) {
		this.delegate = delegate;
		this.repositories = repositories;
	}

	public ClassLoader getLoader(ArtifactIdentifier id, List<URL> repositories)
			throws RavenException {
		return delegate.getLoader(id, repositories);
	}

	public List<URL> getRemoteRepositories() {
		return this.repositories;
	}

	public ClassLoader getParentClassLoader() {
		return delegate.getParentClassLoader();
	}

	public Set<ArtifactIdentifier> getSystemArtifactSet() {
		return delegate.getSystemArtifactSet();
	}

}
