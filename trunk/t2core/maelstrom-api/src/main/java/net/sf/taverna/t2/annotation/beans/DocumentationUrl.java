/**
 * 
 */
package net.sf.taverna.t2.annotation.beans;

import java.net.URL;
import java.util.Set;

import net.sf.taverna.t2.annotation.AppliesTo;
import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * @author alanrw
 *
 */
@AppliesTo(workflowObjectType={Port.class, Activity.class}, many=true)
public interface DocumentationUrl extends WorkflowAnnotation {
	Set<URL> getUrls();
}
