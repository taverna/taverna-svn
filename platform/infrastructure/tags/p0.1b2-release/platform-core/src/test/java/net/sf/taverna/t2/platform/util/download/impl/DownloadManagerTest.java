package net.sf.taverna.t2.platform.util.download.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import net.sf.taverna.t2.platform.pom.impl.ArtifactFileUrlMapper;
import net.sf.taverna.t2.platform.util.download.DownloadManager;
import net.sf.taverna.t2.platform.util.download.URLMapper;

import junit.framework.TestCase;

/**
 * Exercise the download manager's job queue
 * 
 * @author Tom Oinn
 * 
 */
public class DownloadManagerTest extends TestCase {

	public void testSimpleDownloadWithVerification()
			throws MalformedURLException {
		List<URL> sources = new ArrayList<URL>();
		sources
				.add(new URL(
						"http://mirrors.ibiblio.org/pub/mirrors/maven2/activespace/jcache/1.0-dev-3/jcache-1.0-dev-3.jar"));
		File baseLocation = new File("C://test");
		URLMapper mapper = new ArtifactFileUrlMapper(baseLocation,
				new ArtifactIdentifier("test", "test", "testversion"));
		DownloadManager manager = new DownloadManagerImpl(1);
		System.out.println(manager.getAsFile(sources, new Maven2MD5Verifier(),
				mapper, 1));
	}

}
