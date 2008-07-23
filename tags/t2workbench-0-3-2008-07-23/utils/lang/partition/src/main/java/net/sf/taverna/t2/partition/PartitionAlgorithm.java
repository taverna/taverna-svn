package net.sf.taverna.t2.partition;

/**
 * Interface for classes which can partition a set of objects into subsets according
 * to some embedded partitioning rule
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 * 
 * @param ValueType
 *            the java type of values used to represent the distinct partitions
 *            created by this algorithm, in many cases these will be primitive
 *            java types such as String but they could represent ranges of
 *            values in the case of binning of continuous quantities etc.
 */
public interface PartitionAlgorithm<ValueType> {

	/**
	 * Given an object to classify return the value of the partition into which
	 * the object falls.
	 * 
	 * @param newItem
	 * @return
	 */
	ValueType allocate(Object newItem, PropertyExtractorRegistry reg);

}
