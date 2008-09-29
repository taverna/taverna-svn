package net.sf.taverna.t2.reference.impl.external.http;

import java.net.URL;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ValueToReferenceConversionException;
import net.sf.taverna.t2.reference.ValueToReferenceConverterSPI;

/**
 * Convert a URL with http protocol to a HttpReference reference type
 * 
 * @author Tom Oinn
 * 
 */
public class UrlToHttpReference implements ValueToReferenceConverterSPI {

	/**
	 * Can convert if the object is an instance of java.net.URL and the protocol
	 * is HTTP
	 */
	public boolean canConvert(Object o, ReferenceContext context) {
		if (o instanceof URL) {
			if (((URL) o).getProtocol().equalsIgnoreCase("http")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return a new HttpReference constructed from
	 * <code>((URL)o).toExternalForm()</code>
	 */
	public ExternalReferenceSPI convert(Object o, ReferenceContext context)
			throws ValueToReferenceConversionException {
		HttpReference result = new HttpReference();
		result.setHttpUrlString(((URL) o).toExternalForm());
		return result;
	}

}
