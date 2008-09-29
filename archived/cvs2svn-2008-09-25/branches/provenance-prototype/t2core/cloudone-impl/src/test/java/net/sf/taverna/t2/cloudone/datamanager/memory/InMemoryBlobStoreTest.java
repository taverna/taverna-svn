package net.sf.taverna.t2.cloudone.datamanager.memory;

import java.util.HashSet;

import net.sf.taverna.t2.cloudone.datamanager.AbstractBlobStoreTest;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;

/**
 * Test {@link InMemoryBlobStore} using the tests of {@link AbstractBlobStoreTest}.
 *
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public class InMemoryBlobStoreTest extends AbstractBlobStoreTest {

	@Override
	public void setDataManagerAndBlobStore() {
		blobStore = new InMemoryBlobStore();
		dManager = new InMemoryDataManager(TEST_NS,
				new HashSet<LocationalContext>());
	}
}
