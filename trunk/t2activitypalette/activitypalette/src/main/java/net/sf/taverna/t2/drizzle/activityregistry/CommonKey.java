/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyValue;
import net.sf.taverna.t2.drizzle.util.StringKey;
import net.sf.taverna.t2.drizzle.util.StringValue;

/**
 * @author alanrw
 *
 */
public final class CommonKey {
	public static PropertyKey ProcessorClassKey = new StringKey("ProcessorType");
	public static PropertyValue WsdlValue = new StringValue("WSDL");
	public static PropertyValue BiomobyValue = new StringValue("Biomoby");
	public static PropertyKey WsdlLocationKey = new StringKey("WSDLLocation");
	public static PropertyKey WsdlOperationKey = new StringKey("WSDLOperation");
	public static PropertyKey WsdlPortTypeKey = new StringKey("WSDLPortType");
	public static PropertyKey NameKey = new StringKey("Name");
	public static PropertyKey MobyEndpointKey = new StringKey("Moby endpoint");
	public static PropertyKey MobyAuthorityKey = new StringKey("Moby authority");
}
