package net.sf.taverna.t2.activities.wsdl.partition;

import net.sf.taverna.t2.partition.ActivityPartitionAlgorithmSet;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

public class WSDLPartitionAlgorithmSetSPI extends ActivityPartitionAlgorithmSet {

	public WSDLPartitionAlgorithmSetSPI() {
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("operation"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("use"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("style"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("url"));
	}
}
