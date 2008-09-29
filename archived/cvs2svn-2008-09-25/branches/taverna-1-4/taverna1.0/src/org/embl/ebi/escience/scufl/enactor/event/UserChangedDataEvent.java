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

public class UserChangedDataEvent extends WorkflowInstanceEvent {

	private DataThing theDataThing;

	private String oldDataThingID;

	public UserChangedDataEvent(WorkflowInstance workflow,
			String oldDataThingID, DataThing theDataThing) {
		super(workflow);
		this.theDataThing = theDataThing;
		this.oldDataThingID = oldDataThingID;
	}

	/**
	 * Get the DataThing containing the data item which has been changed
	 */
	public DataThing getDataThing() {
		return this.theDataThing;
	}

	/**
	 * Get the ID of the old datathing that has been changed.
	 */
	public String getOldDataThingID() {
		return this.oldDataThingID;
	}

	/**
	 * Pretty toString override
	 */
	public String toString() {
		/*
		 * StringBuffer sb = new StringBuffer();
		 * sb.append("'"+getOriginalLSID()+"' changed to :"); for (int i = 0; i <
		 * collectionLSID.length; i++) { sb.append("\n
		 * '"+collectionLSID[i]+"'"); } return sb.toString();
		 */
		return getOldDataThingID() + " changed to " + getDataThing().toString();
	}

}
