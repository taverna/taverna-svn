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
 * Filename           $RCSfile: ProcessFailureWithInputTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:24 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ng4j;

import java.util.List;
import java.util.Set;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
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

public class ProcessFailureWithInputTest extends
        ProvenanceGeneratorTests {

    static protected final String FAIL_XML = "TomOinn/ConditionalBranchChoice.xml";

    private static final String PROVENANCE_PREFIX = "PREFIX p: <"
            + ProvenanceOntologyConstants.NS + "> ";

    private static final String RDF_PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";

    public ProcessFailureWithInputTest() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void testProcessFailure() throws Exception {
        String inputFile = "TomOinn/ConditionalBranchChoiceInput.xml";
        execute(FAIL_XML, inputFile);

        Set<String> processRuns = getProcessRuns(RUN);
        populateModel(RUN, processRuns);
        // ontModel.write(System.out, "N3");

        String queryString = PROVENANCE_PREFIX + RDF_PREFIX
                + "SELECT ?processRun " + "WHERE { ?processRun p:runsProcess "
                + "<" + ProvenanceGenerator.PROCESS_NS + "Fail_if_true"
                + "> . " + "?processRun rdf:type p:FailedProcessRun . }";
        System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
        Resource failedProcessRun = null;
        try {
            ResultSet results = qexec.execSelect();
            assertTrue("Found failed process run for Fail_if_true", results
                    .hasNext());
            QuerySolution soln = results.nextSolution();
            failedProcessRun = soln.getResource("processRun");
            System.out.println(failedProcessRun.getURI());
            List<String> processInputs = ontology
                    .getProcessInputs(failedProcessRun.getURI());
            for (String input : processInputs) {
                System.out.println("\t input = " + input);
            }
            assertEquals(1, processInputs.size());
        } finally {
            qexec.close();
        }

        queryString = PROVENANCE_PREFIX + " SELECT ?cause "
                + "WHERE { ?processRun p:runsProcess " + " <"
                + ProvenanceGenerator.PROCESS_NS + "Fail_if_true> . "
                + "?processRun p:cause ?cause . }";
        System.out.println(queryString);
        query = QueryFactory.create(queryString);
        qexec = QueryExecutionFactory.create(query, ontModel);
        try {
            ResultSet results = qexec.execSelect();
            assertTrue("Found cause", results.hasNext());
            QuerySolution soln = results.nextSolution();
            Literal cause = soln.getLiteral("cause");
            assertEquals("Test matches, aborting downstream processors", cause
                    .getValue().toString());
        } finally {
            qexec.close();
        }
    }

    protected void tearDown() throws Exception {
        MetadataServiceFactory.getInstance(configuration).removeGraph(RUN);
        super.tearDown();
    }

}
