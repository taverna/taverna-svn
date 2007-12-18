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
 * Filename           $RCSfile: CollectionTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:27 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ng4j;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGeneratorTests;
import uk.org.mygrid.provenance.util.TestUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;

public class CollectionTest extends ProvenanceGeneratorTests {

    public CollectionTest() {
        super();
    }

    /**
     * WARN: Performs actual test only if run independently from the other
     * tests.
     */
    public void testCollection() throws Exception {
        execute("myGrid/collection-example.xml");

        Model instanceData = metadataService.retrieveGraphModel(RUN);
        TestUtils.writeOut(instanceData);
        String triql = "SELECT ?data WHERE <"
                + RUN
                + ">( <urn:lsid:net.sf.taverna:dataCollection:8> "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.CONTAINSDATA)
                + " ?data )";
        Set collection = new HashSet();
        final String dataItem = "urn:lsid:net.sf.taverna:dataItem:";
        collection.add(dataItem + "9");
        collection.add(dataItem + "10");
        collection.add(dataItem + "11");
        int elements = 0;
        System.out.println("Testing the following TriQL query:");
        System.out.println(triql);
        Iterator iterator = executeQuery(triql);
        if (iterator.hasNext()) {
            while (iterator.hasNext()) {
                Map oneResult = (Map) iterator.next();
                Node data = (Node) oneResult.get("data");
                String dataURI = data.getURI();
                assertTrue("dataCollection  = "
                        + " does not contain dataItem = " + dataURI, collection
                        .contains(dataURI));
                elements++;
            }
            assertEquals(3, elements);
        }

        MetadataServiceFactory.getInstance(configuration).removeGraph(RUN);
    }

    protected void tearDown() throws Exception {
        MetadataServiceFactory.getInstance(configuration).removeGraph(RUN);
        super.tearDown();
    }

}
