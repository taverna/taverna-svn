package net.sf.taverna.t2.cloudone.p2p.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.peer.PeerContainer;
import net.sf.taverna.t2.cloudone.peer.PeerProxy;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PeerContainerTest extends AbstractTestCloudOneServer {
	private static File tmpDir;

	@BeforeClass
	public static void makeTmp() throws IOException {
		tmpDir = File.createTempFile("test", "datamanager");
		tmpDir.delete();
		tmpDir.mkdir();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void peer() throws Exception {
		PeerContainer container = new HttpPeerContainer();
		PeerProxy proxy = container.getProxyForNamespace("http2p_" + HOST + "_"
				+ PORT);

		// Add a DataDocument to cloudApp's data manager
		Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
		File newFile = File.createTempFile("test", ".txt");
		FileUtils.writeStringToFile(newFile, "Test data\n", "utf8");
		URL fileURL = newFile.toURI().toURL();
		HttpReferenceScheme urlRef = new HttpReferenceScheme(fileURL);
		references.add(urlRef);
		DataManager dMan = cloudApp.getDataManager();
		DataDocumentIdentifier docId = dMan.registerDocument(references);

		// Retrieve the exported data document over the wire
		Entity<?, ?> retrievedEntity = proxy.export(docId);
		assertTrue("Returned entity was not a DataDocument",
				retrievedEntity instanceof DataDocument);
		DataDocument retrievedDoc = (DataDocument) retrievedEntity;
		assertEquals(references, retrievedDoc.getReferenceSchemes());
		ReferenceScheme ref = retrievedDoc.getReferenceSchemes().iterator()
				.next();
		assertTrue("Reference was not an URLReferenceScheme",
				ref instanceof HttpReferenceScheme);
		assertEquals("URLReference was not " + fileURL, fileURL.toString(),
				((HttpReferenceScheme) ref).getUrl().toString());
	}

	@Ignore("Hard coded identifier, requires server")
	@Test
	public void remoteEmptyList() throws Exception {
		PeerContainer container = new HttpPeerContainer();
		PeerProxy proxy = container.getProxyForNamespace("http2p_"
				+ "nemesis.cs.man.ac.uk" + "_" + 7380);

		EntityListIdentifier listId = EntityIdentifiers
				.parseListIdentifier("urn:t2data:list://http2p_nemesis.cs.man.ac.uk_7380/f2c0db77-3562-442c-af69-8110efabeac5/2");

		// Retrieve the exported data document over the wire
		Entity<?, ?> retrievedEntity = proxy.export(listId);
		assertTrue("Returned entity was not a EntityList",
				retrievedEntity instanceof EntityList);
		EntityList entList = (EntityList) retrievedEntity;
		assertTrue("List was empty", entList.isEmpty());
	}

}
