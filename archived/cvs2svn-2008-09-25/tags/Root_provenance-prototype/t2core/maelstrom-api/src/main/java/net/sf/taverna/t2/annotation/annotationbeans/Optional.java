package net.sf.taverna.t2.annotation.annotationbeans;

import net.sf.taverna.t2.annotation.AppliesTo;
import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.workflowmodel.InputPort;

/**
 * A declaration that the bound input port is optional, if this annotation is
 * refuted then the interpretation should be that the input port is required.
 * 
 * @author Tom Oinn
 * @author Alan Williams
 */
@AppliesTo(targetObjectType = { InputPort.class }, many = false)
public class Optional implements AnnotationBeanSPI {

	/**
	 * Default constructor as mandated by java bean specification
	 */
	public Optional() {
		//
	}

}
