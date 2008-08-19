package net.sf.taverna.t2.activities.biomart.partition;

import net.sf.taverna.t2.partition.ActivityPartitionAlgorithmSet;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

public class BiomartPartitionAlgorithmSetSPI extends
		ActivityPartitionAlgorithmSet {
	
	public BiomartPartitionAlgorithmSetSPI() {
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("url"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("dataset"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("location"));
	}

}
