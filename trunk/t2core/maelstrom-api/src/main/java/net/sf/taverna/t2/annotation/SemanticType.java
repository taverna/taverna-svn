/**
 * 
 */
package net.sf.taverna.t2.annotation;

import net.sf.taverna.t2.workflowmodel.Port;

/**
 * @author alanrw
 *
 */
@AppliesTo(workflowObjectType={Port.class}, many=true)
public interface SemanticType extends OntologyTermAnnotation {

}
