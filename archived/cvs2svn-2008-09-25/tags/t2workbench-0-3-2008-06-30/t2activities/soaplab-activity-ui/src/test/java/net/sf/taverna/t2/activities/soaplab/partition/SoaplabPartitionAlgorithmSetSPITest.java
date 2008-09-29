package net.sf.taverna.t2.activities.soaplab.partition;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.partition.PartitionAlgorithm;
import net.sf.taverna.t2.partition.PartitionAlgorithmSetSPI;
import net.sf.taverna.t2.partition.PartitionAlgorithmSetSPIRegistry;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

import org.junit.Before;
import org.junit.Test;

public class SoaplabPartitionAlgorithmSetSPITest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSPI() {
		List<PartitionAlgorithmSetSPI> list = PartitionAlgorithmSetSPIRegistry.getInstance().getInstances();
		int c=0;
		for (PartitionAlgorithmSetSPI p : list) {
			if (p instanceof SoaplabPartitionAlgorithmSetSPI) {
				c++;
			}
		}
		assertEquals("There should be 1 SoaplabPartitionAlgorithmSetSPI discovered",1,c);
	}
	
	@Test
	public void getPartitonAlgorithms() {
		PartitionAlgorithmSetSPI p = new SoaplabPartitionAlgorithmSetSPI();
		Set<PartitionAlgorithm<?>> set = p.getPartitionAlgorithms();
		assertTrue("should contain an algorithm for 'operation'",set.contains(new LiteralValuePartitionAlgorithm("operation")));
		assertTrue("should contain an algorithm for 'category'",set.contains(new LiteralValuePartitionAlgorithm("category")));
		assertTrue("should contain an algorithm for 'type'",set.contains(new LiteralValuePartitionAlgorithm("type")));
		assertTrue("should contain an algorithm for 'url'",set.contains(new LiteralValuePartitionAlgorithm("url")));
	}
}
