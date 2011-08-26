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
 * Filename           $RCSfile: SyntacticTypeTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:25 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ng4j;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGenerator;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGeneratorTests;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;

public class SyntacticTypeTest extends ProvenanceGeneratorTests {

    public SyntacticTypeTest() {
        super();
    }

    public void testSyntacticType() throws Exception {
        execute(ProvenanceGeneratorTests.BLAST_WORKFLOW);
        Set<String> processRuns = getProcessRuns(RUN);
        populateModel(RUN, processRuns);
        // ontModel.write(System.out, "N3");

        String queryString = ProvenanceGeneratorTests.PROVENANCE_PREFIX
                + "SELECT ?syntacticType "
                + "WHERE { "
                + "<"
                + ProvenanceGenerator.PROCESS_NS
                + "Blast2RDF_out_BLASTRDF> "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.DATA_SYNTACTIC_TYPE
                                .getURI()) + "?syntacticType . }";
        System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
        try {
            ResultSet results = qexec.execSelect();
            assertTrue(results.hasNext());
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Literal syntacticType = soln.getLiteral("syntacticType");
                String syntacticTypeValue = syntacticType.getValue().toString();
                System.out.println(syntacticTypeValue);
                assertTrue(syntacticTypeValue
                        .equals("text/plain,application/rdf+xml")
                        || syntacticTypeValue.equals("text/plain"));

            }
        } finally {
            qexec.close();
        }

        String triql = "SELECT ?syntacticType WHERE ?workflowRun ( "
                + "<"
                + ProvenanceGenerator.WORKFLOW_NS
                + "16OM6H01JJ0_out_result> "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.DATA_SYNTACTIC_TYPE
                                .getURI()) + "?syntacticType) ";
        Iterator iterator = executeQuery(triql);
        while (iterator.hasNext()) {
            Map oneResult = (Map) iterator.next();
            Node syntacticType = (Node) oneResult.get("syntacticType");
            String syntacticTypeValue = syntacticType.getLiteral().getValue()
                    .toString();
            System.out.println(syntacticTypeValue);
            assertEquals("text/plain,application/rdf+xml", syntacticTypeValue);
        }
    }

    protected void tearDown() throws Exception {
        MetadataServiceFactory.getInstance(configuration).removeGraph(RUN);
        super.tearDown();
    }

}
