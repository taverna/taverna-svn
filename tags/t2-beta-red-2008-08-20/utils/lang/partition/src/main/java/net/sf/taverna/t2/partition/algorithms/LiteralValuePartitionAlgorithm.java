package net.sf.taverna.t2.partition.algorithms;

import net.sf.taverna.t2.partition.PartitionAlgorithm;
import net.sf.taverna.t2.partition.PropertyExtractorRegistry;

/**
 * A naive partition algorithm that simply returns the property value it's been
 * configured to use from the property getter.
 * 
 * @author Tom
 * 
 */
public class LiteralValuePartitionAlgorithm implements
		PartitionAlgorithm<Object> {

	private String propertyName = null;
	
	private static String NO_PROPERTY = "No value";
	
	/**
	 * Default constructor. The property name defaults to null, and needs setting using getPropertyName
	 */
	public LiteralValuePartitionAlgorithm() {
		
	}
	
	/**
	 * Constructor that initialised the LiteralValuePartitionAlgorithm with a property name
	 * 
	 * @param propertyName
	 */
	public LiteralValuePartitionAlgorithm(String propertyName) {
		super();
		this.propertyName = propertyName;
	}

	public Object allocate(Object newItem, PropertyExtractorRegistry reg) {
		if (propertyName == null) {
			return NO_PROPERTY;
		}
		else {
			Object propertyValue = reg.getAllPropertiesFor(newItem).get(propertyName);
			if (propertyValue == null) {
				return NO_PROPERTY;
			}
			else {
				return propertyValue;
			}
		}
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	
	
	/**
	 * @return true if obj is a LiteralValuePartionAlgorithm and the property names match
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LiteralValuePartitionAlgorithm) {
			LiteralValuePartitionAlgorithm alg = (LiteralValuePartitionAlgorithm)obj;
			return getPropertyName().equals(alg.getPropertyName());
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getPropertyName().hashCode();
	}

	@Override
	public String toString() {
		return this.propertyName;
	}

}
