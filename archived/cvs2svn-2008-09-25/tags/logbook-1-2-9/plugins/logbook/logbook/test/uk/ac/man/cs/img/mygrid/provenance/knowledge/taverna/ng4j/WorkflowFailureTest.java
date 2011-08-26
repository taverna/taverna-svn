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
 * Filename           $RCSfile: WorkflowFailureTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:24 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ng4j;

import java.util.Iterator;
import java.util.Map;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGeneratorTests;

import com.hp.hpl.jena.graph.Node;

public class WorkflowFailureTest extends ProvenanceGeneratorTests {

    static protected final String WORKFLOW_FAILURE_XML = "myGrid/workflow-failure.xml";

    public WorkflowFailureTest() {
        super();
    }

    public void testProcessFailure() throws Exception {
        execute(WORKFLOW_FAILURE_XML);

        String query = "SELECT ?workflowRun "
                + "WHERE <"
                + RUN
                + "> ( ?workflowRun rdf:type "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.Classes.FAILEDWORKFLOWRUN)
                + " ) USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";

        Iterator iterator = executeQuery(query);
        Map oneResult = (Map) iterator.next();
        Node workflow = (Node) oneResult.get("workflowRun");
        String uri = workflow.getURI();
        System.out.println(uri);
        assertEquals(RUN, uri);
    }

    protected void tearDown() throws Exception {
        MetadataServiceFactory.getInstance(configuration).clear();
    }

}
