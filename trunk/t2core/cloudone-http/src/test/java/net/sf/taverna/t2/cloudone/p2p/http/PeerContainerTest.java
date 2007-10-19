package net.sf.taverna.t2.cloudone.p2p.http;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.PeerContainer;
import net.sf.taverna.t2.cloudone.PeerProxy;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.datamanager.file.FileDataManager;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceScheme;
import net.sf.taverna.t2.cloudone.p2p.HttpPeerContainer;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PeerContainerTest {
	private static File tmpDir;
	
	@BeforeClass
	public static void makeTmp() throws IOException {
		tmpDir = File.createTempFile("test", "datamanager");
		tmpDir.delete();
		tmpDir.mkdir();
	}
	
	@Test
	public void peer() throws Exception {
		String host = "localhost";
		int port = 7381;
		CloudOneApplication cloudApp = new CloudOneApplication(host, port);
		PeerContainer container = new HttpPeerContainer(); // TODO: Create/get one
		PeerProxy proxy = container.getProxyForNamespace("http2p_" + host + "_" + port);
		Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
		File newFile = File.createTempFile("test", ".txt");
		FileUtils.writeStringToFile(newFile, "Test data\n", "utf8");
		URL fileURL = newFile.toURI().toURL();
		URLReferenceScheme urlRef = new URLReferenceScheme(fileURL);
		references.add(urlRef);
		DataManager dMan = cloudApp.getDataManager();
		DataDocumentIdentifier docId = dMan.registerDocument(references);

		// TODO: MAke it work
		//proxy.export(docId); 
	}
	
}
