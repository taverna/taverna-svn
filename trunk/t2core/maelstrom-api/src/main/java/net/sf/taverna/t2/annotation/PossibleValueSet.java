/**
 * 
 */
package net.sf.taverna.t2.annotation;

import java.util.Set;

import net.sf.taverna.t2.workflowmodel.InputPort;

/**
 * @author alanrw
 *
 */
@AppliesTo(workflowObjectType={InputPort.class}, many=false)
public interface PossibleValueSet extends WorkflowAnnotation {
	Set<AnnotationValue> getValues();
}
