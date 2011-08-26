/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.process;

import java.util.ArrayList;
import java.util.List;

/**
 * The OperationGroup class represents an ordered list of Operation objects
 * and allows the extraction of a dynamically resolved view over this list
 * in the form of a list of tree models where the leaf nodes contain concrete
 * operations.<p>
 * The OperationGroup contains an ordered list of OperationFilter objects and
 * a single JobDispatcher object. The JobDispatcher is responsible for taking
 * the filtered list of Operation objects after resolution and applying individual
 * combinations of input data items from the job queue to them, it may do so in
 * serial or parallel forms and may or may not interact with the invoker to handle
 * rescheduling of failed jobs.<p>
 * The JobInvoker is responsible for actually calling the Operation concerned, it
 * may handle aspects such as recursive behaviours and fault tolerance at a per-job
 * level rather than per-process; as such it may request a reschedule of any single
 * job.
 * @author Tom Oinn
 */
public class OperationGroup {

    JobDispatcher jobDispatcher;
    JobInvoker jobInvoker;
    List filterList = new ArrayList();
    List operationTreeList = new ArrayList();

    /**
     * Generates an array of trees of concrete operations where
     * each tree has an OperationTree with a root getUserObject value
     * equal to the corresponding Operation in the OperationGroup 
     */
    public OperationTree[] resolve() {
	return new OperationTree[0];
    }

    /**
     * Return an array of OperationFilter objects which are applied in
     * order to the resolved OperationTree to remove leaf nodes which
     * fail the filtering criteria concerned.
     */
    public OperationFilter[] getFilters() {
	return (OperationFilter[])this.filterList.toArray(new OperationFilter[0]);
    }
    
    /**
     * Set the list of filters, removing any currently in place first
     */
    public void setFilters(OperationFilter[] newFilters) {
	filterList.clear();
	for (int i = 0; i < newFilters.length; i++) {
	    filterList.add(newFilters[i]);
	}
    }

    /**
     * Get the JobDispatcher object associated with this OperationGroup
     */
    public JobDispatcher getDispatcher() {
	return this.jobDispatcher;
    }

    /**
     * Set the JobDispatcher
     */
    public void setDispatcher(JobDispatcher newDispatcher) {
	this.jobDispatcher = newDispatcher;
    }

    /**
     * Get the JobInvoker object associated with this OperationGroup
     */
    public JobInvoker getInvoker() {
	return this.jobInvoker;
    }

    /**
     * Set the JobInvoker
     */
    public void setInvoker(JobInvoker newInvoker) {
	this.jobInvoker = newInvoker;
    }

}
