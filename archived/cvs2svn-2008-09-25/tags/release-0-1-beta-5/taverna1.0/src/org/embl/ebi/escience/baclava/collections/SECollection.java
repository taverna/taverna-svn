/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.collections;

// Utility Imports
import java.util.Collection;

import org.embl.ebi.escience.baclava.collections.SEDataItem;
import java.lang.Object;



/**
 * Provides the standard java Collection interface
 * enriched with additional methods to set and query
 * the semantic metadata associated with each item
 * @author Tom Oinn
 */
public interface SECollection extends Collection {
    
    /**
     * Returns an array of data wrappers
     * enriched with semantic metadata and mime types
     * corresponding to all elements within the
     * collection
     */
    public SEDataItem[] toEnrichedArray();

    /**
     * Returns an array of data wrappers
     * enriched with semantic metadata and mime
     * types corresponding to all elements within
     * the collection where the user object of
     * the data wrapper is of the type passed
     * in as the single parameter
     */
    public SEDataItem[] toEnrichedArray(Object[] a);

    /**
     * Add an enriched data item to this collection
     */
    public void addEnriched(SEDataItem d);

    /**
     * Add a complete enriched collection to this collection
     */
    public void addAllEnriched(SECollection c);

    /**
       For reference, the base Collection interface is 
       as follows :

       // Basic Operations
       int size();
       boolean isEmpty();
       boolean contains(Object element);
       boolean add(Object element);    // Optional
       boolean remove(Object element); // Optional
       Iterator iterator();
       
       // Bulk Operations
       boolean containsAll(Collection c);
       boolean addAll(Collection c);    // Optional
       boolean removeAll(Collection c); // Optional
       boolean retainAll(Collection c); // Optional
       void clear();                    // Optional        
       
       // Array Operations
       Object[] toArray();
       Object[] toArray(Object a[]);
    */

    

}
