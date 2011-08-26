package net.sf.taverna.wsdl.soap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * This class replicates the behaviour of data conversion when using DataThingFactory.bake in Taverna 1.
 * </p>
 * <p>
 * In particular it deals with the conversion of arrays to ArrayList
 * </p>
 * @author Stuart Owen
 *
 */
public class ObjectConverter {

	/**
	 * Converts an Object into an appropriate type, in particular recursively converting [] arrays to List<?>'s.<br>
	 * 
	 * This method is a copy of convertObject in DataThingFactory from Taverna 1
	 * @param theObject
	 * @return
	 */
	public static Object convertObject(Object theObject) {
		if (theObject == null) {
			return null;
		}
		// If an array type...
		Class<?> theClass = theObject.getClass();
		if (theClass.isArray()) {
			// Special case for byte[]
			if (theObject instanceof byte[]) {
				// System.out.println("Found a byte[], returning it.");
				return theObject;
			} else {
				// For all other arrays, create a new
				// List and iterate over the array,
				// unpackaging the item and recursively
				// putting it into the new List after
				// conversion
				Object[] theArray = (Object[]) theObject;
				// System.out.println("Found an array length
				// "+theArray.length+", repacking as List...");
				List<Object> l = new ArrayList<Object>();
				for (int i = 0; i < theArray.length; i++) {
					l.add(convertObject(theArray[i]));
				}
				return l;
			}
		}
		// If a collection, iterate over it and copy
		if (theObject instanceof Collection) {
			if (theObject instanceof List) {
				// System.out.println("Re-packing a list...");
				List<Object> l = new ArrayList<Object>();
				for (Iterator<?> i = ((List<?>) theObject).iterator(); i.hasNext();) {
					l.add(convertObject(i.next()));
				}
				return l;
			} else if (theObject instanceof Set) {
				// System.out.println("Re-packing a set...");
				Set<Object> s = new HashSet<Object>();
				for (Iterator<?> i = ((Set<?>) theObject).iterator(); i.hasNext();) {
					s.add(convertObject(i.next()));
				}
				return s;
			}
		}
		// If a number then return the string representation for it
		if (theObject instanceof Number) {
			// System.out.println("Found a number, converting it to a
			// string...");
			return theObject.toString();
		}
		// Otherwise just return the object
		// System.out.println("Found a "+theObject.getClass().getName()+",
		// returning it");
		return theObject;
	}
}
