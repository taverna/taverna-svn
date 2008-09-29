package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.partition.PartitionAlgorithm;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

public class DefaultPartitionAlgorithms {
	public static List<PartitionAlgorithm<?>> getPartitionAlgorithms() {
		List<PartitionAlgorithm<?>> result = new ArrayList<PartitionAlgorithm<?>>();
		result.add(new LiteralValuePartitionAlgorithm("type"));
		return result;
	}
}
