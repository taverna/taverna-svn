package net.sf.taverna.t2.partition.algorithms;

import net.sf.taverna.t2.partition.PartitionAlgorithmSPI;
import net.sf.taverna.t2.partition.PropertyExtractorRegistry;

/**
 * A naive partition algorithm that simply returns the property value it's been
 * configured to use from the property getter.
 * 
 * @author Tom
 * 
 */
public class LiteralValuePartitionAlgorithm implements
		PartitionAlgorithmSPI<Object> {

	private String propertyName = null;
	
	private static String NO_PROPERTY = "No value";
	
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
	
	@Override
	public String toString() {
		return this.propertyName+"=";
	}

}
