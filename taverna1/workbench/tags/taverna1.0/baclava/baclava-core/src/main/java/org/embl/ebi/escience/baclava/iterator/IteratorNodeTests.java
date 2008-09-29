/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.iterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.embl.ebi.escience.baclava.DataThing;

/**
 * Tests the tree based iterator nodes
 * 
 * @author Tom Oinn
 */
public class IteratorNodeTests {

	public static List colours, shapes, animals;

	static {
		colours = new ArrayList();
		colours.add("Red");
		colours.add("Green");

		shapes = new ArrayList();
		shapes.add("Square");
		shapes.add("Triangle");

		animals = new ArrayList();
		animals.add("Cat");
		animals.add("Mouse");
	}

	public static void main(String[] args) {

		try {

			// First test the basic iterator
			System.out.println("\nBasic iterator node test");
			DataThing coloursThing = new DataThing(colours);
			DataThing animalsThing = new DataThing(animals);
			DataThing shapesThing = new DataThing(shapes);
			printIterator(new BaclavaIteratorNode(coloursThing.iterator("''"),
					"Colour"));

			// Now test a cross product
			System.out.println("\nCross product test (colour*shape)");
			MutableTreeNode t = new JoinIteratorNode();
			t.insert(new BaclavaIteratorNode(coloursThing.iterator("''"),
					"Colour"), 0);
			t.insert(new BaclavaIteratorNode(shapesThing.iterator("''"),
					"Shape"), 0);
			printIterator((ResumableIterator) t);
			System.out
					.println("\nAdding another iterator to that lot (colour*animal*shape)");
			t.insert(new BaclavaIteratorNode(animalsThing.iterator("''"),
					"Animal"), 0);
			printIterator((ResumableIterator) t);

			// And a straight dot product
			System.out.println("\nDot product test (colour.animal)");
			t = new LockStepIteratorNode();
			t.insert(new BaclavaIteratorNode(coloursThing.iterator("''"),
					"Colour"), 0);
			t.insert(new BaclavaIteratorNode(animalsThing.iterator("''"),
					"Animal"), 0);
			printIterator((ResumableIterator) t);

			// And now the cross product of a dot and a normal iterator
			System.out.println("\nCombined ((colour.animal)*shape)");
			MutableTreeNode t2 = new JoinIteratorNode();
			t2.insert(t, 0);
			t2.insert(new BaclavaIteratorNode(shapesThing.iterator("''"),
					"Shape"), 0);
			printIterator((ResumableIterator) t2);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void printIterator(ResumableIterator i) {
		System.out.println("Iterator is of class "
				+ i.getClass().toString().split("\\.")[5] + ", "
				+ ((ResumableIterator) i).size() + " elements, "
				+ ((TreeNode) i).getChildCount() + " children.");
		while (i.hasNext()) {
			// Explode the map and print a new map containing the data objects
			// without the datathing wrapping
			Map display = new HashMap();
			Map current = (Map) i.next();
			for (Iterator k = current.keySet().iterator(); k.hasNext();) {
				Object key = k.next();
				if (current.get(key) instanceof DataThing) {
					DataThing thing = (DataThing) current.get(key);
					display.put(key, thing.getDataObject());
				} else {
					display.put(key, current.get(key));
				}
			}
			int[] location = i.getCurrentLocation();
			StringBuffer sb = new StringBuffer("[");
			for (int j = 0; j < location.length; j++) {
				if (j != 0) {
					sb.append(",");
				}
				sb.append("" + location[j]);
			}
			sb.append("]");
			System.out.println(display + " at location " + sb.toString());
		}
	}

}
