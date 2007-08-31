package net.sf.taverna.t2.cloudone.datamanager;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;

public abstract class AbstractDataManager implements DataManager {

	private Set<LocationalContext> contexts;

	private String namespace;

	public AbstractDataManager(String namespace, Set<LocationalContext> contexts) {
		this.namespace = namespace;
		this.contexts = contexts;
	}

	public String getCurrentNamespace() {
		return namespace;
	}

	public Set<LocationalContext> getLocationalContexts() {
		return contexts;
	}

	public List<String> getManagedNamespaces() {
		return Collections.singletonList(namespace);
	}

	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg) {
		return registerError(depth, implicitDepth, msg, null);
	}

	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			Throwable throwable) {
		return registerError(depth, implicitDepth, null, throwable);
	}
}
