package net.sf.taverna.t2.partition;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

/**
 * A base class for an PartitionAlgorithmSetSPI to contain the properties common to all activities
 * @author Stuart Owen
 *
 */
public abstract class ActivityPartitionAlgorithmSet implements PartitionAlgorithmSetSPI {
	
	/**
	 * A Set of the PartitionAlgorithms populated when at class instantiation
	 */
	protected Set<PartitionAlgorithm<?>> partitonAlgorithms = new HashSet<PartitionAlgorithm<?>>();

	public ActivityPartitionAlgorithmSet() {
		partitonAlgorithms.add(new LiteralValuePartitionAlgorithm("type"));
	}



	public Set<PartitionAlgorithm<?>> getPartitonAlgorithms() {
		return partitonAlgorithms;
	}
}

