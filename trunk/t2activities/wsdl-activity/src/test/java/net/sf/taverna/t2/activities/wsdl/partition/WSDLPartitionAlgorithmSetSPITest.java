package net.sf.taverna.t2.activities.wsdl.partition;


import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.partition.PartitionAlgorithm;
import net.sf.taverna.t2.partition.PartitionAlgorithmSetSPI;
import net.sf.taverna.t2.partition.PartitionAlgorithmSetSPIRegistry;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

import org.junit.Before;
import org.junit.Test;

public class WSDLPartitionAlgorithmSetSPITest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSPI() {
		List<PartitionAlgorithmSetSPI> list = PartitionAlgorithmSetSPIRegistry.getInstance().getInstances();
		int c=0;
		for (PartitionAlgorithmSetSPI p : list) {
			if (p instanceof WSDLPartitionAlgorithmSetSPI) {
				c++;
			}
		}
		assertEquals("There should be 1 WSDLPartitionAlgorithmSetSPI discovered",1,c);
	}
	
	@Test
	public void getPartitonAlgorithms() {
		PartitionAlgorithmSetSPI p = new WSDLPartitionAlgorithmSetSPI();
		Set<PartitionAlgorithm<?>> set = p.getPartitonAlgorithms();
		assertTrue("should contain an algorithm for 'operation'",set.contains(new LiteralValuePartitionAlgorithm("operation")));
		assertTrue("should contain an algorithm for 'style'",set.contains(new LiteralValuePartitionAlgorithm("style")));
		assertTrue("should contain an algorithm for 'use'",set.contains(new LiteralValuePartitionAlgorithm("use")));
		assertTrue("should contain an algorithm for 'type'",set.contains(new LiteralValuePartitionAlgorithm("type")));
	}
}
