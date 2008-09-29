/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.lsid;

import org.embl.ebi.escience.baclava.LSIDProvider;
import org.embl.ebi.escience.baclava.LSIDProvider.NamespaceEnumeration;

/**
 * A ridiculously over simplified implementation of the LSIDProvider interface,
 * just returns strings appended with a counter. IDs will be unique within a
 * single JVM instance but certainly not beyond it.
 * 
 * @author Tom Oinn
 */
public class StupidLSIDProvider implements LSIDProvider {

	static int count = 0;

	/**
	 * Default constructor so an instance can be created for use by the enactor
	 * framework
	 */
	public StupidLSIDProvider() {
		//
	}

	/**
	 * Returns a particularly dumb implementation of a unique identifier
	 */
	public synchronized String getID(LSIDProvider.NamespaceEnumeration namespace) {
		return "urn:lsid:net.sf.taverna:" + namespace.toString() + ":"
				+ (count++);
	}

}
