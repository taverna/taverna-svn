package net.sf.taverna.t2.activities.stringconstant.partition;


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

public class StringConstantPartitionAlgorithmSetSPITest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSPI() {
		List<PartitionAlgorithmSetSPI> list = PartitionAlgorithmSetSPIRegistry.getInstance().getInstances();
		int c=0;
		for (PartitionAlgorithmSetSPI p : list) {
			if (p instanceof StringConstantPartitionAlgorithmSetSPI) {
				c++;
			}
		}
		assertEquals("There should be 1 SoaplabPartitionAlgorithmSetSPI discovered",1,c);
	}
	
	@Test
	public void getPartitonAlgorithms() {
		PartitionAlgorithmSetSPI p = new StringConstantPartitionAlgorithmSetSPI();
		Set<PartitionAlgorithm<?>> set = p.getPartitionAlgorithms();
		assertEquals("There should be 1 partition algorithm in the set",1,set.size());
		assertTrue("should contain an algorithm for 'type'",set.contains(new LiteralValuePartitionAlgorithm("type")));
		
	}
}
