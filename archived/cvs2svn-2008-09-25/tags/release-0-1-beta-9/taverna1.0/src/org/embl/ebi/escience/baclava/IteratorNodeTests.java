/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava;

import java.util.*;
import javax.swing.tree.*;

/**
 * Tests the tree based iterator nodes
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
	    printIterator(new BaclavaIteratorNode(new BaclavaIterator(colours), "Colour"));
	    
	    
	    // Now test a cross product
	    System.out.println("\nCross product test (colour*shape)");
	    MutableTreeNode t = new JoinIteratorNode();
	    t.insert(new BaclavaIteratorNode(new BaclavaIterator(colours), "Colour"),0);
	    t.insert(new BaclavaIteratorNode(new BaclavaIterator(shapes), "Shape"),0);
	    printIterator((Iterator)t);
	    System.out.println("\nAdding another iterator to that lot (colour*animal*shape)");
	    t.insert(new BaclavaIteratorNode(new BaclavaIterator(animals), "Animal"),0);
	    printIterator((Iterator)t);
	    
	    // And a straight dot product
	    System.out.println("\nDot product test (colour.animal)");
	    t = new LockStepIteratorNode();
	    t.insert(new BaclavaIteratorNode(new BaclavaIterator(colours), "Colour"),0);
	    t.insert(new BaclavaIteratorNode(new BaclavaIterator(animals), "Animal"),0);
	    printIterator((Iterator)t);

	    // And now the cross product of a dot and a normal iterator
	    System.out.println("\nCombined ((colour.animal)*shape)");
	    MutableTreeNode t2 = new JoinIteratorNode();
	    t2.insert(t,0);
	    t2.insert(new BaclavaIteratorNode(new BaclavaIterator(shapes), "Shape"),0);
	    printIterator((Iterator)t2);

	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    private static void printIterator(Iterator i) {
	System.out.println("Iterator is of class "+i.getClass().toString().split("\\.")[5]+", "+((ResumableIterator)i).size()+" elements, "+((TreeNode)i).getChildCount()+" children.");
	while (i.hasNext()) {
	    System.out.println(i.next());
	}
    }
    

}
