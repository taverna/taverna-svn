package net.sf.taverna.t2.activities.soaplab.partition;

import net.sf.taverna.t2.partition.ActivityPartitionAlgorithmSet;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

/**
 * Partition algorithm set for Soaplab, contains properties for category and operation.
 * 
 * @author Stuart Owen
 *
 */
public class SoaplabPartitionAlgorithmSetSPI extends ActivityPartitionAlgorithmSet {

	public SoaplabPartitionAlgorithmSetSPI() {
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("category"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("operation"));
	}
}
