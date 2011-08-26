/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.apache.wsif.schema;

import javax.xml.namespace.QName;

/**
 * Slightly nasty hack to get around the package level
 * permission on the getXMLAttribute call in SequenceElement.
 * We need this to be able to get the max/minOccurs properties
 * from the schema.
 * @author Tom Oinn
 */
public class PermissionHack {

    public static QName getXMLAttribute(SequenceElement se, QName name) {
	return se.getXMLAttribute(name);
    }

}
