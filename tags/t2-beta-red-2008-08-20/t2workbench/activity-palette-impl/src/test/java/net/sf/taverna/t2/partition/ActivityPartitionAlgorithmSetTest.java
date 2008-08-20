package net.sf.taverna.t2.partition;

import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

import org.junit.Before;
import org.junit.Test;

public class ActivityPartitionAlgorithmSetTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetPartitonAlgorithms() {
		PartitionAlgorithmSetSPI set = new ActivityPartitionAlgorithmSet() {
			
		};
		
		assertTrue("Should contain an algorithm for type",set.getPartitionAlgorithms().contains(new LiteralValuePartitionAlgorithm("type")));
	}

}
