/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.factory;

import java.util.*;
import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.semantics.*;


/**
 * A collection of static methods to build DataThings
 * from various other Java object types.
 * @author Tom Oinn
 */
public class DataThingFactory {
    
    /**
     * Easy for String objects, everything already recognizes
     * them so no custom code required.
     */
    public static DataThing bake(String theString) {
	return new DataThing(theString);
    }
    
    /**
     * For String arrays convert the array to a List and store
     * that.
     */
    public static DataThing bake(String[] theStringArray) {
	List theList = new ArrayList();
	for (int i = 0; i < theStringArray.length; i++) {
	    theList.add(theStringArray[i]);
	}
	return new DataThing(theList);
    }

    /**
     * For byte arrays store the byte array as is
     */
    public static DataThing bake(byte[] theByteArray) {
	return new DataThing(theByteArray);
    }

}
