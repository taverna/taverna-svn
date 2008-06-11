package net.sf.taverna.t2.partition;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PartitionAlgorithmSPIRegistryTest {
	private static PartitionAlgorithmSPIRegistry registry;
	
	@BeforeClass
	public static void setUp() {
		registry = PartitionAlgorithmSPIRegistry.getInstance();
	}

	@Test
	public void testGetInstance() {
		List<PartitionAlgorithmSPI> list = registry.getInstances();
		assertTrue("There should be more than one PartitionAlgorithm Found",list.size()>0);
		boolean found = false;
		for (PartitionAlgorithmSPI item : list) {
			if (item instanceof DummyPartitionAlgorithm) {
				found=true;
				break;
			}
		}
		assertTrue("There should have been a DummyPartitionAlgorithm",found);
	}
	
	@Test
	public void testGetByType() {
		List<PartitionAlgorithmSPI<?>> list = registry.getByType(DummyPartitionAlgorithm.class);
		assertEquals("There should only be 1 DummyPartitionAlgorithm",1,list.size());
		assertTrue("There PartitionAlgorithm should be a DummyPartitionAlgorithm",list.get(0) instanceof DummyPartitionAlgorithm);
	}
}
