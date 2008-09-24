/*
 * RepositoryType.java
 *
 * Created on March 7, 2005, 2:53 PM
 */

package uk.ac.man.cs.img.fetaEngine.store.load;

import uk.ac.man.cs.img.fetaEngine.util.AbstractEnumeration;

/**
 * 
 * @author alperp
 */
public class RepositoryType extends AbstractEnumeration {

	public RepositoryType(String toString) {
		super(toString);

	}

	public static final RepositoryType WEB = new RepositoryType("web");

	public static final RepositoryType FILE = new RepositoryType("file");

	public static final RepositoryType UDDI = new RepositoryType("uddi");

	public static RepositoryType getTypeForString(String typeStr) {

		if (typeStr.equalsIgnoreCase(UDDI.toString())) {
			return UDDI;
		} else if (typeStr.equalsIgnoreCase(WEB.toString())) {
			return WEB;
		} else if (typeStr.equalsIgnoreCase(FILE.toString())) {
			return FILE;
		} else {
			return null;
		}

	}
} // RepositoryType
