package net.sf.taverna.t2.activities.rshell.partition;

import net.sf.taverna.t2.partition.ActivityPartitionAlgorithmSet;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

public class RshellPartitionAlgorithmSetSPI extends ActivityPartitionAlgorithmSet{
	
	public RshellPartitionAlgorithmSetSPI() {
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("operation"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("category"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("url"));
	}

}
