/**
 * 
 */
package net.sf.taverna.t2.annotation;

import net.sf.taverna.t2.workflowmodel.InputPort;

/**
 * @author alanrw
 *
 */
@AppliesTo(workflowObjectType={InputPort.class}, many=false)
public interface DefaultSetting extends WorkflowAnnotation {
	/**
	 * @return can be null
	 */
	AnnotationValue getDefaultValue();
}
