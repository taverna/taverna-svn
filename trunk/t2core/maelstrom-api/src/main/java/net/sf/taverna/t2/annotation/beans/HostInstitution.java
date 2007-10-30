/**
 * 
 */
package net.sf.taverna.t2.annotation.beans;

import net.sf.taverna.t2.annotation.AppliesTo;
import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * @author alanrw
 *
 */
//TODO
@AppliesTo(workflowObjectType={Activity.class}, many=false)
public interface HostInstitution extends WorkflowAnnotation {

}
