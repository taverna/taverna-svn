package net.sf.taverna.t2.partition;

import static org.junit.Assert.*;

import java.util.Set;

import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ActivityPartitionAlgorithmSetTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	@Ignore
	public void testGetPartitonAlgorithms() {
		PartitionAlgorithmSetSPI set = new ActivityPartitionAlgorithmSet() {

			public Set<PartitionAlgorithm<?>> getPartitionAlgorithms() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		
		assertTrue("Should contain an algorithm for type",set.getPartitionAlgorithms().contains(new LiteralValuePartitionAlgorithm("type")));
	}

}
