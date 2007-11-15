/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.StringKey;

/**
 * @author alanrw
 *
 */
public final class CommonKey {
	public static final PropertyKey LocalServiceWorkerClassKey = new StringKey("WorkerClass");
	public static final PropertyKey SoaplabEndpointKey = new StringKey("Endpoint");
	public static final PropertyKey StringConstantValueKey = new StringKey("Value");
	public static final PropertyKey WorkflowDefinitionURLKey = new StringKey("DefinitionURL");
	public static PropertyKey ProcessorClassKey = new StringKey("ProcessorType"); //$NON-NLS-1$
	public static PropertyKey WsdlLocationKey = new StringKey("WSDLLocation"); //$NON-NLS-1$
	public static PropertyKey WsdlOperationKey = new StringKey("WSDLOperation"); //$NON-NLS-1$
	public static PropertyKey WsdlPortTypeKey = new StringKey("WSDLPortType"); //$NON-NLS-1$
	public static PropertyKey NameKey = new StringKey("Name"); //$NON-NLS-1$
	public static PropertyKey MobyEndpointKey = new StringKey("Moby endpoint"); //$NON-NLS-1$
	public static PropertyKey MobyAuthorityKey = new StringKey("Moby authority"); //$NON-NLS-1$
}
