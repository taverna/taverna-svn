package net.sf.taverna.t2.platform.util.download.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import net.sf.taverna.t2.platform.pom.impl.PomParserImpl;
import net.sf.taverna.t2.platform.util.download.DownloadManager;

import junit.framework.TestCase;

public class PomParserImplTest extends TestCase {

	public static void testEvilPomOfDoom() throws MalformedURLException {
		URL baseURL = new URL("http://mirrors.ibiblio.org/pub/mirrors/maven2/");
		List<URL> repositories = new ArrayList<URL>();
		repositories.add(baseURL);
		DownloadManager manager = new DownloadManagerImpl(1);
		PomParserImpl parser = new PomParserImpl();
		File fileCache = new File("C:/testParser");
		parser.setDownloadManager(manager);
		parser.setFileCache(fileCache);
		System.out.println(parser.getDescription(new ArtifactIdentifier("org.apache.cxf", "apache-cxf",
				"2.1.2"), repositories));
	}
	
	public static void testEvilPomOfDoom2() throws MalformedURLException {
		URL baseURL = new URL("http://mirrors.ibiblio.org/pub/mirrors/maven2/");
		URL mygridURL = new URL("http://www.mygrid.org.uk/maven/repository/");
		URL mygridSnapshotURL = new URL("http://www.mygrid.org.uk/maven/snapshot-repository/");
		List<URL> repositories = new ArrayList<URL>();
		repositories.add(baseURL);
		repositories.add(mygridURL);
		repositories.add(mygridSnapshotURL);
		DownloadManager manager = new DownloadManagerImpl(1);
		PomParserImpl parser = new PomParserImpl();
		File fileCache = new File("C:/testParser");
		parser.setDownloadManager(manager);
		parser.setFileCache(fileCache);
		System.out.println(parser.getDescription(new ArtifactIdentifier("uk.org.mygrid.taverna.processors", "all",
				"1.7.2-SNAPSHOT"), repositories));
	}
}
