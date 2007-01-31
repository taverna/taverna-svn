package net.sf.taverna.raven.spi;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractArtifactFilter implements ArtifactFilter {

	private List<ArtifactFilterListener> artifactFilterListeners = new ArrayList<ArtifactFilterListener>();

	public AbstractArtifactFilter() {
		super();
	}

	public void addArtifactFilterListener(ArtifactFilterListener listener) {
		synchronized (artifactFilterListeners) {
			if (!artifactFilterListeners.contains(listener)) {
				artifactFilterListeners.add(listener);
			}
		}
	}

	public void removeArtifactFilterListener(ArtifactFilterListener listener) {
		synchronized (artifactFilterListeners) {
			artifactFilterListeners.remove(listener);
		}
	}

	protected void fireFilterChanged(ArtifactFilter filter) {
		synchronized (artifactFilterListeners) {
			for (ArtifactFilterListener listener : artifactFilterListeners) {
				listener.filterChanged(filter);
			}
		}
	}

}