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
public interface Optionality extends WorkflowAnnotation {
	boolean getOptionality();
}
