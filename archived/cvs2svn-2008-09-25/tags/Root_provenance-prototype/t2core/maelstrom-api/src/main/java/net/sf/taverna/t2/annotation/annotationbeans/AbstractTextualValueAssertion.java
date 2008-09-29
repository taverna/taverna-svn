package net.sf.taverna.t2.annotation.annotationbeans;

import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.annotation.AppliesTo;

/**
 * Generic bit of free text that can be stuck to anything, subclass for more
 * specific uses
 * 
 * @author Tom Oinn
 * 
 */
@AppliesTo(targetObjectType = { Object.class }, many = true)
public abstract class AbstractTextualValueAssertion implements AnnotationBeanSPI {

	private String text;

	/**
	 * Default constructor as mandated by java bean specification
	 */
	protected AbstractTextualValueAssertion() {
		//
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
