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
 * Filename           $RCSfile: ProcessFailureTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:25 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ng4j;

import java.util.List;
import java.util.Set;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceCreationException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGenerator;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGeneratorTests;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class ProcessFailureTest extends ProvenanceGeneratorTests {

    static protected final String FAIL_XML = "myGrid/collection-failure.xml";
    
    public ProcessFailureTest()
            throws MetadataServiceCreationException {
        super();
    }

    public void testProcessFailure() throws Exception {
        execute(FAIL_XML);
        Set<String> processRuns = getProcessRuns(RUN);
        populateModel(RUN, processRuns);

        // ontModel.write(System.out, "N3");

        String queryString = ProvenanceGeneratorTests.PROVENANCE_PREFIX
                + ProvenanceGeneratorTests.RDF_PREFIX + "SELECT ?processRun "
                + "WHERE { ?processRun p:runsProcess " + "<"
                + ProvenanceGenerator.PROCESS_NS + "CollectionFailure" + "> . "
                + "?processRun rdf:type p:FailedProcessRun . }";
        System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
        Resource failedProcessRun = null;
        try {
            ResultSet results = qexec.execSelect();
            assertTrue("Found process run for CollectionFailure", results
                    .hasNext());
            QuerySolution soln = results.nextSolution();
            failedProcessRun = soln.getResource("processRun");
            System.out.println(failedProcessRun.getURI());
            List<String> processInputs = ontology
                    .getProcessInputs(failedProcessRun.getURI());
            for (String input : processInputs) {
                System.out.println("\t input = " + input);
            }
            assertEquals(2, processInputs.size());
        } finally {
            qexec.close();
        }

        queryString = ProvenanceGeneratorTests.PROVENANCE_PREFIX
                + " SELECT ?cause " + "WHERE { ?processRun p:runsProcess "
                + " <" + ProvenanceGenerator.PROCESS_NS
                + "CollectionFailure> . " + "?processRun p:cause ?cause . }";
        System.out.println(queryString);
        query = QueryFactory.create(queryString);
        qexec = QueryExecutionFactory.create(query, ontModel);
        try {
            ResultSet results = qexec.execSelect();
            assertTrue("Found cause", results.hasNext());
            QuerySolution soln = results.nextSolution();
            Literal cause = soln.getLiteral("cause");
            System.out.println(cause);
            assertEquals(
                    "Output port 'output' not populated by service instance",
                    cause.getValue().toString());
        } finally {
            qexec.close();
        }
    }

    protected void tearDown() throws Exception {
        MetadataServiceFactory.getInstance(configuration).removeGraph(RUN);
        super.tearDown();
    }

}
