package net.sf.taverna.t2.matlabactivity.partition;

import net.sf.taverna.t2.partition.ActivityPartitionAlgorithmSet;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

/**
 *
 * @author petarj
 */
public class MatPluginPartitionAlgorithmSetSPI extends ActivityPartitionAlgorithmSet {

    public MatPluginPartitionAlgorithmSetSPI() {
        partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("operation"));
        partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("category"));
        partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("url"));
    }
}
