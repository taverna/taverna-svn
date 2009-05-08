package net.sf.taverna.t2.platform.pom.impl;

import java.io.File;
import java.net.URL;
import java.util.List;

import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import net.sf.taverna.t2.platform.pom.JarManager;
import net.sf.taverna.t2.platform.util.download.DownloadManager;
import net.sf.taverna.t2.platform.util.download.DownloadVerifier;
import net.sf.taverna.t2.platform.util.download.impl.Maven2MD5Verifier;

/**
 * Simple implementation of JarManager, wrapping static methods in the
 * PomParserImpl
 * 
 * @author Tom Oinn
 * 
 */
public class JarManagerImpl implements JarManager {

	private DownloadManager manager;
	private File cache;
	private DownloadVerifier verifier;

	public JarManagerImpl() {
		this.verifier = new Maven2MD5Verifier();
	}

	/**
	 * Inject a download manager to be used by this instance.
	 */
	public void setDownloadManager(DownloadManager dm) {
		this.manager = dm;
	}

	/**
	 * Set the file cache location for artifact files
	 */
	public void setFileCache(File cacheLocation) {
		this.cache = cacheLocation;
	}

	/**
	 * Fetch the jar file for a specified artifact identifier. Uses local
	 * repositories if available first before trying non local repositories in
	 * order.
	 */
	public File getArtifactJar(ArtifactIdentifier id, List<URL> repositories) {
		return PomParserImpl.getFile(id, repositories, this.manager,
				this.verifier, this.cache, "jar");
	}

}
