/**
 * 
 */
package net.sf.taverna.t2.annotation.annotationbeans;

import net.sf.taverna.t2.annotation.AppliesTo;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * The host institution for an activity implementation
 * 
 * @author Tom Oinn
 * @author Alan Williams
 */
@AppliesTo(targetObjectType = { Activity.class }, many = false)
public class HostInstitution extends AbstractTextualValueAssertion {

	/**
	 * Default constructor as mandated by java bean specification
	 */
	public HostInstitution() {
		super();
	}

}
