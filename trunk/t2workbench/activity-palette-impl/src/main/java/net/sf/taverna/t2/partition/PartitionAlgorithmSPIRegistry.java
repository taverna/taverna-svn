package net.sf.taverna.t2.partition;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;

/**
 * An SPI Registry that discovered PartitionAlgorithms, which are used to define the partitioning
 * within the Activity Palette.
 * <p>
 * The registry is a singleton factory class and should be accessed through the getInstance method.
 * </p>
 * <p>
 * The PartitionAlgorithmSPI classes to be discovered need to be defined in a resource named
 * <pre>
 * META-INF/services/net.sf.taverna.t2.partition.PartitionAlgorithmSPI
 * </pre>
 * </p>
 * 
 * @author Stuart Owen
 * @see PartitionAlgorithmSPI
 */
@SuppressWarnings("unchecked")
public class PartitionAlgorithmSPIRegistry extends SPIRegistry<PartitionAlgorithmSPI> {

	private static PartitionAlgorithmSPIRegistry instance = new PartitionAlgorithmSPIRegistry();


	public static PartitionAlgorithmSPIRegistry getInstance() {
		return instance;
	}
	
	private PartitionAlgorithmSPIRegistry() {
		super(PartitionAlgorithmSPI.class);
	}
	
	public List<PartitionAlgorithmSPI<?>> getByType(Class<?>type) {
		List<PartitionAlgorithmSPI<?>> result = new ArrayList<PartitionAlgorithmSPI<?>>();
		for (PartitionAlgorithmSPI<?> instance : getInstances()) {
			if (instance.getClass().isAssignableFrom(type)) {
				result.add(instance);
			}
		}
		return result;
	}
	
}
