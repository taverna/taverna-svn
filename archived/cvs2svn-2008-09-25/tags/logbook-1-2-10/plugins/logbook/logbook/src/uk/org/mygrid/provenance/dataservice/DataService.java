/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: DataService.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:07 $
 *               by   $Author: stain $
 * Created on 03-May-2006
 *****************************************************************/
package uk.org.mygrid.provenance.dataservice;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.store.DuplicateLSIDException;
import org.embl.ebi.escience.baclava.store.NoSuchLSIDException;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * Data service for Taverna {@link org.embl.ebi.escience.baclava.DataThing}s.
 * 
 * @author dturi
 * @version $Id: DataService.java,v 1.1 2007-12-14 12:49:07 stain Exp $
 */
public interface DataService {

    /**
     * Destroys all previously stored data.
     */
    public abstract void clear() throws DataServiceException;

    /**
     * Stores <code>dataThing</code>, suppressing exceptions if
     * <code>silent</code> is <code>true</code>.
     * 
     * @param dataThing
     * @param silent
     *            a boolean flag to control whether storage is silent.
     * @throws DuplicateLSIDException
     * @throws DataServiceException
     */
    public abstract void storeDataThing(DataThing dataThing, boolean silent)
            throws DuplicateLSIDException, DataServiceException;

    /**
     * Store a workflow that is being run. Note that the LSID is unique to each
     * run and is not the LSID stored in the workflow.
     * 
     * @param lsid
     *            Unique LSID for this workflow run
     * @param model
     *            The scufl model that is being run
     * @throws DataServiceException
     */
    public void storeWorkflow(String lsid, ScuflModel model)
            throws DataServiceException;

    /**
     * Fetch a DataThing from the given LSID
     * 
     * @param lsid
     * @return The datathing
     * @throws NoSuchLSIDException
     *             If the datathing is not found
     * @throws DataServiceException
     *             If the data service is not working properly
     */
    public abstract DataThing fetchDataThing(String lsid)
            throws NoSuchLSIDException, DataServiceException;

    /**
     * Fetch a workflow given the lsid (workflowRunID) and puts it in model.
     * Note that this LSID is unique to each run and is not the LSID stored in
     * the workflow.
     * 
     * @param lsid
     *            param model a Scufl model of the workflow
     * @throws DataServiceException
     *             If the data service is not working properly or if the
     *             workflow is not found
     * @throws ScuflException
     *             If the workflow could not be parsed or loaded by Scufl
     */
    public void populateWorkflowModel(String lsid, ScuflModel model)
            throws DataServiceException, ScuflException;

    /**
     * Fetch a workflow as an unparsed XML string given the lsid
     * (workflowRunID). Note that this LSID is unique to each run and is not the
     * LSID stored in the workflow.
     * 
     * @param lsid
     * @return An XML String representing the Scufl model of the workflow
     * @throws DataServiceException
     *             If the data service is not working properly or if the
     *             workflow is not found
     */
    public String fetchUnparsedWorkflow(String lsid)
            throws DataServiceException;

    /**
     * Does the <code>lsid</code> exist in a concrete form?
     * 
     * @param lsid
     * @return <code>true</code> if the lsid has data.
     * @throws DataServiceException
     */
    public abstract boolean hasData(String lsid) throws DataServiceException;

}