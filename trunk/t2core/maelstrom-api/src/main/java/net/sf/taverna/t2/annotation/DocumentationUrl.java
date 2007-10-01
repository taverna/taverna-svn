/**
 * 
 */
package net.sf.taverna.t2.annotation;

import java.net.URL;
import java.util.Set;

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
