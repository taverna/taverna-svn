package net.sf.taverna.raven.spi;

import java.util.ArrayList;

public abstract class AbstractArtifactFilter implements ArtifactFilter {

	private ArrayList<ArtifactFilterListener> artifactFilterListeners = new ArrayList<ArtifactFilterListener>();

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

	@SuppressWarnings("unchecked")
	protected void fireFilterChanged(ArtifactFilter filter) {
		synchronized (artifactFilterListeners) {
			ArrayList<ArtifactFilterListener> listeners =
				(ArrayList<ArtifactFilterListener>) artifactFilterListeners.clone();
			for (ArtifactFilterListener listener : listeners) {
				listener.filterChanged(filter);
			}
		}
	}

}