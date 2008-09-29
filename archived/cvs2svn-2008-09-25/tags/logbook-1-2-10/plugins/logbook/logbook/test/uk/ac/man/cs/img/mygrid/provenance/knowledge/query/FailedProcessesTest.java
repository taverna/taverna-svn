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
 * Filename           $RCSfile: FailedProcessesTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:20 $
 *               by   $Author: stain $
 * Created on 18-Aug-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.query;

import java.util.Collection;
import java.util.Map;

import junit.framework.TestCase;

import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.implementation.SimpleUserContext;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.JenaMetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGenerator;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.WorkflowEnactorHelper;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.util.DataObject;
import uk.org.mygrid.provenance.util.TestUtils;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author dturi
 * @version $Id: FailedProcessesTest.java,v 1.1 2007-12-14 12:53:20 stain Exp $
 */
public class FailedProcessesTest extends TestCase {

    private static final String FAILED_PROCESS = ProvenanceGenerator.PROCESS_NS
            + "Fail_if_true1";

    public static void main(String[] args) {
        junit.textui.TestRunner.run(FailedProcessesTest.class);
    }

    public void testMyFailedProcesses() throws Exception {
        FailedProcesses myFailedProcesses = new FailedProcesses();
        System.out.println(myFailedProcesses);
    }

    public void testInputDataOfFailedProcesses() throws Exception {
        UserContext userContext = new SimpleUserContext(
                TestUtils.TEST_PERSON_1,
                TestUtils.LAB_1, "urn:lsid:removeme");
        new WorkflowEnactorHelper().executeWorkflow("myGrid/failure-test.xml",
                userContext);
        FailedProcesses failedProcesses = new FailedProcesses();
        JenaMetadataService rdfRepository = failedProcesses.getRdfRepository();
        System.out.println(rdfRepository
                .retrieveGraph(TestUtils.RUN));
        Map inputDataOfFailedProcesses = failedProcesses
                .inputDataOfFailedProcessesInWorkflow(TestUtils.RUN);
        Collection value = (Collection) inputDataOfFailedProcesses
                .get(FAILED_PROCESS);
        assertNotNull(FAILED_PROCESS + " not amongst failed processes", value);
        assertTrue(FAILED_PROCESS + " has no input data", !value.isEmpty());
        // if (value != null)
        assertEquals("urn:lsid:net.sf.taverna:dataItem:7", value.iterator()
                .next().toString());
        System.out.println(inputDataOfFailedProcesses);

        Model instanceData = rdfRepository
                .retrieveGraphModel(TestUtils.RUN);
        DataObject dataView = new JenaProvenanceOntology(instanceData)
                .dataView("urn:lsid:net.sf.taverna:dataItem:7");
        System.out.println(dataView);
    }

}
