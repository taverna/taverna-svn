/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.iterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;

/**
 * Tests the functionality of the various Baclava iterator widgets
 * 
 * @author Tom Oinn
 */
public class IteratorTest {

	public static void main(String[] args) {
		try {
			// Create a new DataThing out of a set of lists of strings...
			Set s = new HashSet();
			s.add(new String[] { "foo", "bar", "urgle" });
			s.add(new String[] { "wibble", "wurble", "wobble", "flurgle" });
			DataThing thing = DataThingFactory.bake(s);
			System.out.println("Created datathing, syntax type is "
					+ thing.getSyntacticType());
			String desiredType = "l('text/plain')";
			for (BaclavaIterator i = thing.iterator(desiredType); i.hasNext();) {
				DataThing insideThing = (DataThing) i.next();
				System.out.println("Iterated, type of nested thing is "
						+ insideThing.getSyntacticType());
			}
			// Create another data thing with a list of strings
			DataThing thing2 = DataThingFactory.bake(new String[] { "1", "2",
					"3", "4", "5", "6" });
			String desiredType2 = "'text/plain'";

			for (Iterator i = new JoinIterator(
					new BaclavaIterator[] { thing.iterator(desiredType),
							thing2.iterator(desiredType2) }); i.hasNext();) {
				Object[] row = (Object[]) i.next();
				System.out.println("Found a row...");
				for (int j = 0; j < row.length; j++) {
					System.out.println("  "
							+ ((DataThing) row[j]).getDataObject().toString());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
