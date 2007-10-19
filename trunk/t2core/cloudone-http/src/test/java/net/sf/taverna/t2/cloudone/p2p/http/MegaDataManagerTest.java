package net.sf.taverna.t2.cloudone.p2p.http;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collections;

import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.PeerContainer;
import net.sf.taverna.t2.cloudone.datamanager.MegaDataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.p2p.HttpPeerContainer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MegaDataManagerTest {

	private static CloudOneApplication cloudApp;


	@Test
	public void megaDM() throws Exception {
		InMemoryDataManager mem1 = new InMemoryDataManager("fish", Collections
				.<LocationalContext> emptySet());
		InMemoryDataManager mem2 = new InMemoryDataManager("soup", Collections
				.<LocationalContext> emptySet());
		MegaDataManager megaDM = new MegaDataManager(mem1);

		EntityListIdentifier emptyList1 = mem1.registerEmptyList(2);
		EntityListIdentifier emptyList2 = mem2.registerEmptyList(2);
		assertNotNull(megaDM.getEntity(emptyList1));
		try {
			megaDM.getEntity(emptyList2);
			fail("Did not throw NotFoundException for " + emptyList2);
		} catch (NotFoundException e) {
			// Expected
		}

	}
	
	@BeforeClass
	public static void startServer() {
		cloudApp = new CloudOneApplication("localhost",
				7381);
		cloudApp.startServer();
	}

	@AfterClass
	public static void stopServer() {
		cloudApp.stopServer();
	}

	@Test
	public void addPeer() throws RetrievalException, NotFoundException {

		InMemoryDataManager inMemoryDataManager = new InMemoryDataManager(
				"fish", Collections.<LocationalContext> emptySet());
		MegaDataManager megaDM = new MegaDataManager(inMemoryDataManager);
		EntityListIdentifier emptyList1 = cloudApp.getDataManager()
				.registerEmptyList(2);
		PeerContainer peer = new HttpPeerContainer();
		megaDM.addPeer(peer);
//		assertNotNull("Entity was null", megaDM.getEntity(emptyList1));
	}

}
