package net.sf.taverna.t2.annotation.annotationbeans;

import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.annotation.AppliesTo;

/**
 * Generic annotation containing a pair of numeric values with precision
 * determined by the type parameter which form a bound.
 * 
 * @author Tom Oinn
 * 
 */
@AppliesTo(targetObjectType = { Object.class }, many = true)
public abstract class AbstractNumericRangeAssertion<NumericType extends Number>
		implements AnnotationBeanSPI {

	private NumericType upperNumericValue;

	private NumericType lowerNumericValue;
	
	/**
	 * Default constructor as mandated by java bean specification
	 */
	protected AbstractNumericRangeAssertion() {
		//
	}

	public NumericType getUpperNumericValue() {
		return upperNumericValue;
	}

	public void setUpperNumericValue(NumericType upperNumericValue) {
		this.upperNumericValue = upperNumericValue;
	}

	public NumericType getLowerNumericValue() {
		return lowerNumericValue;
	}

	public void setLowerNumericValue(NumericType lowerNumericValue) {
		this.lowerNumericValue = lowerNumericValue;
	}

}
