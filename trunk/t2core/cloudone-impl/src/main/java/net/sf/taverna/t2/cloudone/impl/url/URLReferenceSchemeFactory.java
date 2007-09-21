package net.sf.taverna.t2.cloudone.impl.url;

import net.sf.taverna.t2.cloudone.impl.AbstractReferenceSchemeFactory;

/**
 * Reference scheme factory for the URLReferenceScheme. Defines the following
 * context keys through metadata file:
 * <p>
 * MachineName = name
 * <p>
 * NetworkName = subnet, mask, name
 * <p>
 * 'name' tokens are free strings, subnet and mask are specified as in IPv4 i.e.
 * in the form a.b.c.d where a,b,c,d are decimal numbers in the range 0-255
 * inclusive.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public class URLReferenceSchemeFactory extends AbstractReferenceSchemeFactory<URLReferenceScheme> {

	private static URLReferenceSchemeFactory urlRefSchemeFactory;
	
	protected URLReferenceSchemeFactory() {
		super();
	}
	
	public static URLReferenceSchemeFactory getInstance() {
		if (urlRefSchemeFactory==null) {
			urlRefSchemeFactory = new URLReferenceSchemeFactory();
		} 
		return urlRefSchemeFactory;
	}
	
	private URLReferenceScheme getReferenceScheme() {
		URLReferenceScheme ref = new URLReferenceScheme();
		return ref;
	}
}
