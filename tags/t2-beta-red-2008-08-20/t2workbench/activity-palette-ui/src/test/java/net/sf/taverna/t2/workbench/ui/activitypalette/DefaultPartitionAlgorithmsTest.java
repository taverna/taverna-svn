package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.util.List;

import net.sf.taverna.t2.partition.PartitionAlgorithm;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

import org.junit.Test;
import static org.junit.Assert.*;

public class DefaultPartitionAlgorithmsTest {
	
	@Test
	public void testDefaults() {
		List<PartitionAlgorithm<?>> list = DefaultPartitionAlgorithms.getPartitionAlgorithms();
		assertEquals("There should be 1 default",1,list.size());
		assertTrue("It should contain a partition algorithm for type",list.contains(new LiteralValuePartitionAlgorithm("type")));
		
	}

}
