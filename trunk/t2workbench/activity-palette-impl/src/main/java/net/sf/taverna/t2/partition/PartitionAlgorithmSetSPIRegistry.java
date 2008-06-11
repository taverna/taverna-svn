package net.sf.taverna.t2.partition;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;

/**
 * An SPI Registry that discovered PartitionAlgorithmSets, which are used to define the partitioning
 * within the Activity Palette.
 * <p>
 * The registry is a singleton factory class and should be accessed through the getInstance method.
 * </p>
 * <p>
 * The PartitionAlgorithmSetSPI classes to be discovered need to be defined in a resource named
 * <pre>
 * META-INF/services/net.sf.taverna.t2.partition.PartitionAlgorithmSetSPI
 * </pre>
 * </p>
 * 
 * @author Stuart Owen
 * @see PartitionAlgorithm
 * @see PartitionAlgorithmSetSPI
 */
@SuppressWarnings("unchecked")
public class PartitionAlgorithmSetSPIRegistry extends SPIRegistry<PartitionAlgorithmSetSPI> {

	private static PartitionAlgorithmSetSPIRegistry instance = new PartitionAlgorithmSetSPIRegistry();


	public static PartitionAlgorithmSetSPIRegistry getInstance() {
		return instance;
	}
	
	private PartitionAlgorithmSetSPIRegistry() {
		super(PartitionAlgorithmSetSPI.class);
	}
	
}
