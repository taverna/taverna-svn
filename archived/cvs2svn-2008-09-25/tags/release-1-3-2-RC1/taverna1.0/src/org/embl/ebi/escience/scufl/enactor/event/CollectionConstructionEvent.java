/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;
import java.util.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.enactor.*;
import org.embl.ebi.escience.baclava.*;

public class CollectionConstructionEvent extends WorkflowInstanceEvent {
    
    private String originalLSID;
    private String[] collectionLSID;

    public CollectionConstructionEvent(WorkflowInstance workflow,
				       String[] collectionLSID,
				       String originalLSID) {
	super(workflow);
	this.collectionLSID = collectionLSID;
	this.originalLSID = originalLSID;
    }
    
    /**
     * Get the LSIDs corresponding to the new collection structure
     * wrapping the original item. This returns an array of strings
     * which will contain as many members as the wrapping was deep.
     * So, if the input was a string and the desired output was
     * a list of lists this value will contain two LSID strings, the first
     * being the outermost collection LSID and the second being that
     * of the collection within this that contains the actual wrapped
     * value. If the wrapping were deeper there would be more items, in
     * most cases this is likely to be only one level deep so this
     * array will contain a single string.
     */
    public String[] getCollectionLSIDs() {
	return this.collectionLSID;
    }

    /**
     * Get the LSID of the data item which has been wrapped
     */
    public String getOriginalLSID() {
	return this.originalLSID;
    }

    /**
     * Pretty toString override
     */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("'"+getOriginalLSID()+"' wrapped in :");
	for (int i = 0; i < collectionLSID.length; i++) {
	    sb.append("\n  '"+collectionLSID[i]+"'");
	}
	return sb.toString();
    }
}
