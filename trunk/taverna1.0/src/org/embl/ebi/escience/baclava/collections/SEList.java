/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.collections;

// Utility Imports
import java.util.Collection;

import org.embl.ebi.escience.baclava.collections.SECollection;
import java.lang.Object;



/**
 * Provides a parallel interface to the collections
 * List interface which contains additional metadata
 * enabled operations.
 * @author Tom Oinn
 */
public interface SEList extends SECollection {
    
    /**
     * Provide List functionality
     */
    public Object get(int index);
    public Object set(int index, Object element);
    public void add(int index, Object element);
    public Object remove(int index);

    /**
     * Inserts all of the elements in the specified collection into this 
     * list at the specified position (optional operation). Shifts the 
     * element currently at that position (if any) and any subsequent 
     * elements to the right (increases their indices). The new elements 
     * will appear in this list in the order that they are returned by the 
     * specified collection's iterator. The behavior of this operation is 
     * unspecified if the specified collection is modified while the 
     * operation is in progress. (Note that this will occur if the 
     * specified collection is this list, and it's nonempty.)
     */
    public boolean addAll(int index, Collection c);

}
