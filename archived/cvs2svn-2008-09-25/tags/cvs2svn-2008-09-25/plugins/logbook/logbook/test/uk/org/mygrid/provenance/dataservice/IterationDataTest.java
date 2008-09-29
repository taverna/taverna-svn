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
 * Filename           $RCSfile: IterationDataTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:52:58 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.org.mygrid.provenance.dataservice;

import java.util.Iterator;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.store.NoSuchLSIDException;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventListener;
import org.embl.ebi.escience.scufl.enactor.implementation.SimpleUserContext;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.OldProvenanceGenerator;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGeneratorTests;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.TestConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.WorkflowEnactorHelper;

public class IterationDataTest extends ProvenanceGeneratorTests {

    static protected final String ITERATION_EXAMPLE = "IterationStrategyExample.xml";

    public IterationDataTest() {
        super();
    }

    public void testIteration() throws Exception {
        UserContext userContext = new SimpleUserContext(TEST_PERSON_2, LAB_2,
                "urn:lsid:removeme", "zapp", "angelica");
        OldProvenanceGenerator provenanceGenerator = new OldProvenanceGenerator(
                configuration);
        DataService dataService = DataServiceFactory.getInstance(configuration);
        dataService.clear();
        boolean isReinitialised = false;
        try {
            dataService.fetchDataThing(TestConstants.EXAMPLE_DATA_ITEM);
        } catch (NoSuchLSIDException e) {
            isReinitialised = true;
        }
        assertTrue("Data tables reinitialised", isReinitialised);
        WorkflowEnactorHelper.executeWorkflow(
                ITERATION_EXAMPLE, userContext,
                new WorkflowEventListener[] { provenanceGenerator });
        
        DataThing dataItem = dataService.fetchDataThing(TestConstants.EXAMPLE_DATA_ITEM);
        assertEquals("greenrabbit", dataItem.getDataObject());
        assertEquals("'text/plain'", dataItem.getSyntacticType());

        DataThing dataCollection;
        try {
            dataCollection = dataService
                    .fetchDataThing(TestConstants.EXAMPLE_DATA_COLLECTION);
            assertEquals("l('text/plain')", dataCollection.getSyntacticType());
            Iterator childIterator = dataCollection.childIterator();
            assertTrue("data collection not empty", childIterator.hasNext());
            while (childIterator.hasNext()) {
                DataThing dataThing = (DataThing) childIterator.next();
                System.out.println(dataThing.getDataObject());
            }
        } catch (NoSuchLSIDException e) {
            System.err.println("Not tested for collection - retry");
        }
    }

    protected void tearDown() throws Exception {
        MetadataServiceFactory.getInstance(configuration).removeGraph(
                RUN);
        super.tearDown();
    }
}
