/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.factory;

import java.util.*;
import org.embl.ebi.escience.baclava.*;


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

    /**
     * For arrays of byte arrays store each byte array
     * in a List
     */
    public static DataThing bake(byte[][] theByteArrayArray) {
	List theList = new ArrayList();
	for (int i = 0; i < theByteArrayArray.length; i++) {
	    theList.add(theByteArrayArray[i]);
	}
	return new DataThing(theList);
    }

    /**
     * Bake a List of Lists into a List of byte[]
     */
    public static DataThing bakeForSoaplab(List theList) {
	Object[] list = ((List)theList).toArray();
	if (list.length == 0) {
	    // Return an empty data thing
	    return new DataThing(null);
	}
	if (!(list[0] instanceof List)) {
	    // If not a list of lists then just return the
	    // original object wrapped in a DataThing
	    return new DataThing(theList);
	}
		
	Vector v = new Vector();
	for (int i = 0; i < list.length; i++) {
	    Object[] list2 = ((ArrayList)list[i]).toArray();
	    if (list2.length > 0 && (list2[0] instanceof Byte)) {
		byte[] bytes = new byte [list2.length];
		for (int j = 0; j < list2.length; j++)
		    bytes[j] = ((Byte)list2[j]).byteValue();
		v.addElement (bytes);
	    } 
	    else {
		// If we can't cope here just return the original
		// object wrapped up in a DataThing
		return new DataThing(theList);
	    }
	}
	byte[][] results = new byte[v.size()][];
	v.copyInto(results);
	return bake(results);
    }

}
