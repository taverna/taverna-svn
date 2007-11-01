/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class WsdlProcessorFactoryDecoder implements PropertyDecoder {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.activityregistry.PropertyDecoder#canDecode(java.lang.Object)
	 */
	public boolean canDecode(Object encodedObject) {
		boolean result = (encodedObject instanceof WSDLBasedProcessorFactory);
		return result;
	}

}
