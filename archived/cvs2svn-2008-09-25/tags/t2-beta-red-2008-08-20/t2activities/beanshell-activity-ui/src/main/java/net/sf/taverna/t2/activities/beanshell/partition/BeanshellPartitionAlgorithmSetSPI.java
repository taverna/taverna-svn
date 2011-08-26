package net.sf.taverna.t2.activities.beanshell.partition;

import net.sf.taverna.t2.partition.ActivityPartitionAlgorithmSet;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

public class BeanshellPartitionAlgorithmSetSPI extends ActivityPartitionAlgorithmSet{
	
	public BeanshellPartitionAlgorithmSetSPI() {
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("operation"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("category"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("url"));
	}

}
