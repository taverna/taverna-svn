package net.sf.taverna.t2.activities.biomoby.partition;

import net.sf.taverna.t2.partition.ActivityPartitionAlgorithmSet;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

public class BiomobyPartitionAlgorithmSetSPI extends ActivityPartitionAlgorithmSet {
	
	public BiomobyPartitionAlgorithmSetSPI() {
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("authority"));
	}
	
}
