package net.sf.taverna.t2.partition;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PartitionAlgorithmSPIRegistryTest {
	private static PartitionAlgorithmSetSPIRegistry registry;
	
	@BeforeClass
	public static void setUp() {
		registry = PartitionAlgorithmSetSPIRegistry.getInstance();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetInstance() {
		List<PartitionAlgorithmSetSPI> list = registry.getInstances();
		assertTrue("There should be at least 1 item in the list",list.size()>0);
		boolean found = false;
		for (PartitionAlgorithmSetSPI item : list) {
			if (item instanceof DummyPartitionAlgorithmSet) {
				found=true;
				break;
			}
		}
		assertTrue("There should have been a DummyPartitionAlgorithmSet",found);
	}
}
