package net.sf.taverna.t2.cloudone.datamanager.memory;

import java.util.HashSet;

import org.junit.Before;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManagerTest;





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

	@Before
	public void setDataManager() {
		dManager = new InMemoryDataManager(TEST_NS,
					new HashSet<LocationalContext>());
	}
	
}
