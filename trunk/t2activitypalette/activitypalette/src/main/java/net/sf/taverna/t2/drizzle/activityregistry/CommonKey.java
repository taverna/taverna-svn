/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.StringKey;

/**
 * @author alanrw
 *
 */
public final class CommonKey {
	public static final PropertyKey LocalServiceWorkerClassKey = new StringKey("WorkerClass"); //$NON-NLS-1$
	public static final PropertyKey SoaplabEndpointKey = new StringKey("Endpoint"); //$NON-NLS-1$
	public static final PropertyKey StringConstantValueKey = new StringKey("Value"); //$NON-NLS-1$
	public static final PropertyKey WorkflowDefinitionURLKey = new StringKey("DefinitionURL"); //$NON-NLS-1$
	public static final PropertyKey ProcessorClassKey = new StringKey("ProcessorType"); //$NON-NLS-1$
	public static final PropertyKey WsdlLocationKey = new StringKey("WSDLLocation"); //$NON-NLS-1$
	public static final PropertyKey WsdlOperationKey = new StringKey("WSDLOperation"); //$NON-NLS-1$
	public static final PropertyKey WsdlPortTypeKey = new StringKey("WSDLPortType"); //$NON-NLS-1$
	public static final PropertyKey NameKey = new StringKey("Name"); //$NON-NLS-1$
	public static final PropertyKey MobyEndpointKey = new StringKey("Moby endpoint"); //$NON-NLS-1$
	public static final PropertyKey MobyAuthorityKey = new StringKey("Moby authority"); //$NON-NLS-1$
	public static final PropertyKey LocalServiceCategoryKey = new StringKey("Category"); //$NON-NLS-1$
	public static final PropertyKey SoaplabCategoryKey = new StringKey("Category"); //$NON-NLS-1$
	

}
