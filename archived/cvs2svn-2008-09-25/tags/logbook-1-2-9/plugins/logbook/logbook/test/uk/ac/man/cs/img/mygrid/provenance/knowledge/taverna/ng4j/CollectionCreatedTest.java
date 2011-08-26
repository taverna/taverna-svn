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
 * Filename           $RCSfile: CollectionCreatedTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:24 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ng4j;

import java.util.Set;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGeneratorTests;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

public class CollectionCreatedTest extends ProvenanceGeneratorTests {

    public CollectionCreatedTest() {
        super();
    }

    public void testCollectionCreated() throws Exception {
        execute("myGrid/iteration-crossproduct.xml");
        Set<String> processRuns = getProcessRuns(RUN);
        populateModel(RUN, processRuns);
        
        ontModel.write(System.out, "N3");

        assertTrue(ontology
                .isDataCollection("urn:lsid:net.sf.taverna:dataCollection:8"));
        assertTrue(ontology
                .isDataCollection("urn:lsid:net.sf.taverna:dataCollection:7"));
        assertTrue(!ontology
                .isDataCollection("urn:lsid:net.sf.taverna:dataItem:6"));

        String queryString = ProvenanceGeneratorTests.PROVENANCE_PREFIX
                + "SELECT ?dataCollection "
                + "WHERE { <urn:lsid:net.sf.taverna:dataItem:6> "
                + "p:dataWrappedInto ?dataCollection . }";
        System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
        try {
            ResultSet results = qexec.execSelect();
            assertTrue("Found dataWrappedInto", results.hasNext());
            QuerySolution soln = results.nextSolution();
            Resource dataCollection = soln.getResource("dataCollection");
            System.out.println(dataCollection.getURI());
        } finally {
            qexec.close();
        }
        //
        // String triql = "SELECT ?data WHERE
        // (<urn:lsid:net.sf.taverna:dataCollection:7:1> "
        // + ProvenanceOntology
        // .bracketify(ProvenanceOntologyConstants.ObjectProperties.CONTAINSDATA)
        // + " ?data )";
        // Set collection = new HashSet();
        // final String dataItem = "urn:lsid:net.sf.taverna:dataItem:";
        // collection.add(dataItem + "8:1");
        // collection.add(dataItem + "9:1");
        // collection.add(dataItem + "10:1");
        // int elements = 0;
        // Iterator iterator = executeQuery(triql);
        // while (iterator.hasNext()) {
        // Map oneResult = (Map) iterator.next();
        // Node data = (Node) oneResult.get("data");
        // String dataURI = data.getURI();
        // assertTrue("dataCollection = " + " contains dataItem = " + dataURI,
        // collection.contains(dataURI));
        // elements++;
        // }
        // assertEquals(3, elements);
    }

    protected void tearDown() throws Exception {
        MetadataServiceFactory.getInstance(configuration).removeGraph(RUN);
        super.tearDown();
    }

}
