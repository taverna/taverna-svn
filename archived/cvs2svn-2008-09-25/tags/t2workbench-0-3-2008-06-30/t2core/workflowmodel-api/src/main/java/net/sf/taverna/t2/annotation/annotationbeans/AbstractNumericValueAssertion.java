package net.sf.taverna.t2.annotation.annotationbeans;

import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.annotation.AppliesTo;

/**
 * Generic annotation containing a single number of precision specified by the
 * type variable
 * 
 * @author Tom Oinn
 * 
 */
@AppliesTo(targetObjectType = { Object.class }, many = true)
public abstract class AbstractNumericValueAssertion<NumericType extends Number>
		implements AnnotationBeanSPI {

	private NumericType numericValue;

	/**
	 * Default constructor as mandated by java bean specification
	 */
	protected AbstractNumericValueAssertion() {
		//
	}

	public NumericType getNumericValue() {
		return numericValue;
	}

	public void setNumericValue(NumericType numericValue) {
		this.numericValue = numericValue;
	}

}
