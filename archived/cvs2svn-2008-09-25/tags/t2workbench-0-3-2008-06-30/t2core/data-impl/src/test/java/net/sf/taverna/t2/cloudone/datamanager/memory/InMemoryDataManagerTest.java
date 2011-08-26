package net.sf.taverna.t2.cloudone.datamanager.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManagerTest;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

import org.junit.Before;
import org.junit.Test;


/**
 * Tests the {@link InMemoryDataManager} implementation of the
 * {@link DataManager}. In addition to the {@link AbstractDataManagerTest} this
 * testcase will assert identifier style of the {@link InMemoryDataManager}.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class InMemoryDataManagerTest extends AbstractDataManagerTest {

	@SuppressWarnings("unchecked")
	@Test
	public void checkDocumentCounter() throws NotFoundException, StorageException, RetrievalException {
		Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
		DataDocumentIdentifier docId = dManager.registerDocument(references);
		assertEquals("urn:t2data:ddoc://" + TEST_NS + "/data0", docId
				.toString());
	}
	@Test
	public void checkListCounter() throws StorageException {
		int depth = 1;
		EntityListIdentifier list0 = dManager.registerEmptyList(depth);
		EntityListIdentifier list1 = dManager.registerEmptyList(depth);
		assertFalse(list0.equals(list1));
		// InMemoryDataManager has a naive counter, list0, list1, etc.
		assertEquals("urn:t2data:list://" + TEST_NS + "/list1/1/f", list1
				.toString());
	}
	
	@Override
	@Before
	public void setDataManager() {
		dManager = new InMemoryDataManager(TEST_NS,
					new HashSet<LocationalContext>());
	}
	
}
