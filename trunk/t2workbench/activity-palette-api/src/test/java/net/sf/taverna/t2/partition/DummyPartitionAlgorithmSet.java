package net.sf.taverna.t2.partition;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

public class DummyPartitionAlgorithmSet implements PartitionAlgorithmSetSPI {

	public Set<PartitionAlgorithm<?>> getPartitonAlgorithms() {
		Set<PartitionAlgorithm<?>> result = new HashSet<PartitionAlgorithm<?>>();

		LiteralValuePartitionAlgorithm p = new LiteralValuePartitionAlgorithm(
				"dummy");
		result.add(p);
		return result;
	}

}
