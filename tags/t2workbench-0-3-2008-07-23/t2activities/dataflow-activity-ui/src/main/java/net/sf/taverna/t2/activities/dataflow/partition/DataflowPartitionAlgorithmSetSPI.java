package net.sf.taverna.t2.activities.dataflow.partition;

import net.sf.taverna.t2.partition.ActivityPartitionAlgorithmSet;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

public class DataflowPartitionAlgorithmSetSPI extends ActivityPartitionAlgorithmSet {

	public DataflowPartitionAlgorithmSetSPI() {
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("type"));
	}
}
