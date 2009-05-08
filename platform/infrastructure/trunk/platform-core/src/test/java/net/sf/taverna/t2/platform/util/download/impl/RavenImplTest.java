package net.sf.taverna.t2.platform.util.download.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import net.sf.taverna.t2.platform.pom.JarManager;
import net.sf.taverna.t2.platform.pom.PomParser;
import net.sf.taverna.t2.platform.pom.impl.JarManagerImpl;
import net.sf.taverna.t2.platform.pom.impl.PomParserImpl;
import net.sf.taverna.t2.platform.raven.Raven;
import net.sf.taverna.t2.platform.raven.impl.RavenImpl;
import net.sf.taverna.t2.platform.util.download.DownloadManager;
import junit.framework.TestCase;

/**
 * Test the new implementation of raven-like functionality
 * 
 * @author Tom Oinn
 */
public class RavenImplTest extends TestCase {

	String cacheLocation = "C:/testRaven";
	List<URL> repositories;
	JarManager jarManager;
	DownloadManager downloadManager;
	PomParser pomParser;
	Raven raven;

	public void testRaven() throws MalformedURLException {
		// raven.getLoader(new ArtifactIdentifier("uk.org.mygrid.resources",
		// "freefluo-taverna-exts", "1.7.2-SNAPSHOT"), repositories);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		URL baseURL = new URL("http://mirrors.ibiblio.org/pub/mirrors/maven2/");
		URL mygridURL = new URL("http://www.mygrid.org.uk/maven/repository/");
		URL mygridSnapshotURL = new URL(
				"http://www.mygrid.org.uk/maven/snapshot-repository/");
		repositories = new ArrayList<URL>();
		repositories.add(baseURL);
		repositories.add(mygridURL);
		repositories.add(mygridSnapshotURL);
		downloadManager = new DownloadManagerImpl(1);
		PomParserImpl parserImp = new PomParserImpl();
		File fileCache = new File(cacheLocation);
		parserImp.setDownloadManager(downloadManager);
		parserImp.setFileCache(fileCache);
		JarManagerImpl jarManagerImp = new JarManagerImpl();
		jarManagerImp.setDownloadManager(downloadManager);
		jarManagerImp.setFileCache(fileCache);
		jarManager = jarManagerImp;
		pomParser = parserImp;
		raven = new RavenImpl(Thread.currentThread().getContextClassLoader(),
				pomParser, jarManager, new HashSet<ArtifactIdentifier>(),
				new ArrayList<URL>());
	}
}
