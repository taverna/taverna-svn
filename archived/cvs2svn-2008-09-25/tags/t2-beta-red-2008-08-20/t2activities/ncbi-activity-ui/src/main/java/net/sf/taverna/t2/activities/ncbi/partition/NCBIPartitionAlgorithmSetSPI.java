package net.sf.taverna.t2.activities.ncbi.partition;

import net.sf.taverna.t2.partition.ActivityPartitionAlgorithmSet;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

public class NCBIPartitionAlgorithmSetSPI extends ActivityPartitionAlgorithmSet{
	
	public NCBIPartitionAlgorithmSetSPI() {
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("operation"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("category"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("url"));
	}
}
