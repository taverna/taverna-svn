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
 * Last modified on   $Date: 2007-12-14 12:53:27 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ng4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGeneratorTests;
import uk.org.mygrid.provenance.util.TestUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;

public class DibertTest extends ProvenanceGeneratorTests {

	String query;

	public DibertTest() {
		super();
	}

	public void testExecution() throws Exception {

		execute(ProvenanceGeneratorTests.DILBERT_WORKFLOW);

		Model instanceData = metadataService.retrieveGraphModel(RUN);
		TestUtils.writeOut(instanceData);

		Iterator iterator;

		query = "SELECT ?initialLSID, ?title, ?author, ?description "
				+ "WHERE <"
				+ RUN
				+ "> ( ?workflow "
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
								.getURI()) + " ?description) ";
		iterator = executeQuery(query);

		Map oneResult = (Map) iterator.next();
		Node title = (Node) oneResult.get("title");
		Node author = (Node) oneResult.get("author");
		Node description = (Node) oneResult.get("description");
		Node initialLSID = (Node) oneResult.get("initialLSID");
		assertEquals("Fetch today's Dilbert comic", title.getLiteral()
				.getValue().toString());
		assertEquals("Tom Oinn", author.getLiteral().getValue().toString());
		assertEquals("urn:lsid:www.mygrid.org.uk:operation:" + "VI9FMF5HBQ3",
				initialLSID.getLiteral().getValue().toString());
		assertEquals(
				"Use the local java plugins and some filtering operations to fetch the comic strip image from http://www.dilbert.com",
				description.getLiteral().getValue().toString());

		String getComicStrip = "urn:www.mygrid.org.uk/process#getComicStrip";
		query = "SELECT * WHERE ( <" + RUN + "> <"
				+ ProvenanceVocab.EXECUTED_PROCESS_RUN.getURI()
				+ "> ?processRunId ) ( ?processRunId <"
				+ ProvenanceVocab.RUNS_PROCESS.getURI() + "> <" + getComicStrip
				+ "> . ?processRunId <"
				+ ProvenanceVocab.PROCESS_INPUT
				+ "> ?processInput . ?processInput <"
				+ ProvenanceVocab.INPUT_DATA_HAS_NAME.getURI()
				+ "> ?processInputName ) ";
		iterator = executeQuery(query);
		List<String> inputs = new ArrayList<String>();
		while (iterator.hasNext()) {
			Map sol = (Map) iterator.next();
			String processInput = sol.get("processInput").toString();
			String processInputName = sol.get("processInputName").toString();
			inputs.add(processInputName);
			System.out.println(metadataService.getFirstObjectPropertyValue(processInput,
					ProvenanceVocab.INPUT_DATA_HAS_NAME.getURI()));
		}
		assertEquals(4, inputs.size());
		assertTrue(inputs.contains("urn:www.mygrid.org.uk/process#getComicStrip_in_base"));
		assertTrue(inputs.contains("urn:www.mygrid.org.uk/process#getComicStrip_in_url"));
	}

	protected void tearDown() throws Exception {
		MetadataServiceFactory.getInstance(configuration).removeGraph(RUN);
		super.tearDown();
	}

}
