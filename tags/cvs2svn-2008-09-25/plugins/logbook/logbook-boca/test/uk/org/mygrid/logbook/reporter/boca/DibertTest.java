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
 * Filename           $RCSfile: DibertTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:59:52 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.org.mygrid.logbook.reporter.boca;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.org.mygrid.logbook.reporter.LogBookReporterTests;
import uk.org.mygrid.provenance.util.TestUtils;

import com.hp.hpl.jena.rdf.model.Model;

public class DibertTest extends LogBookReporterTests {

	String query;

	public DibertTest() {
		super();
	}

	public void testExecution() throws Exception {
		//MetadataServiceFactory.getInstance(configuration).clear();
		execute(DILBERT_WORKFLOW);

		Model instanceData = metadataService.retrieveGraphModel(RUN);
		TestUtils.writeOut(instanceData);

		query = "SELECT ?initialLSID ?title ?author ?description "
				+ "WHERE { GRAPH <"
				+ RUN
				+ "> { ?workflow "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.WORKFLOW_TITLE.getURI())
				+ " ?title . ?workflow "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.WORKFLOW_INITIAL_LSID
								.getURI())
				+ " ?initialLSID . ?workflow "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.WORKFLOW_AUTHOR.getURI())
				+ " ?author . ?workflow "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.WORKFLOW_DESCRIPTION
								.getURI()) + " ?description } } ";
		TupleQueryResult querySolution = executeQuery(query, RUN);
		BindingSet bindingSet = querySolution.next();
		Value title = bindingSet.getValue("title");
		Value author = bindingSet.getValue("author");
		Value description = bindingSet.getValue("description");
		Value initialLSID = bindingSet.getValue("initialLSID");
		assertEquals("Fetch today's Dilbert comic", title.toString());
		assertEquals("Tom Oinn", author.toString());
		assertEquals("urn:lsid:www.mygrid.org.uk:operation:" + "VI9FMF5HBQ3",
				initialLSID.toString());
		assertEquals(
				"Use the local java plugins and some filtering operations to fetch the comic strip image from http://www.dilbert.com",
				description.toString());

	}

	protected void tearDown() throws Exception {
		MetadataServiceFactory.getInstance(configuration).removeGraph(RUN);
		super.tearDown();
	}

}
