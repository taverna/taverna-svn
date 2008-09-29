package net.sf.taverna.t2.partition;

import java.util.Set;

/**
 * An SPI interface that provides access to a Set of partition algorithms.
 * 
 * @author Stuart Owen
 *
 */
public interface PartitionAlgorithmSetSPI {
	/**
	 * @return a Set of PartitionAlgorithms
	 */
	public Set<PartitionAlgorithm<?>> getPartitionAlgorithms();
}
