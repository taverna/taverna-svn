package net.sf.taverna.t2.cloudone.datamanager.memory;

import java.util.HashSet;

import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.AbstractBlobStoreTest;

public class InMemoryblobStoreTest extends AbstractBlobStoreTest {

	@Override
	public void setDataManagerAndBlobStore() {
		blobStore = new InMemoryBlobStore();
		dManager = new InMemoryDataManager(TEST_NS,
				new HashSet<LocationalContext>());
	}
}
