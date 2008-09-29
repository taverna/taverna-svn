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
 * Filename           $RCSfile: LogBookReporterTests.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:59:52 $
 *               by   $Author: stain $
 * Created on 20-Jun-2005
 *****************************************************************/
package uk.org.mygrid.logbook.reporter;

import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.implementation.SimpleUserContext;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.openrdf.query.TupleQueryResult;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.LogLevel;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.WorkflowEnactorHelper;
import uk.org.mygrid.logbook.metadataservice.BocaRemoteMetadataService;
import uk.org.mygrid.provenance.util.LogBookConfigurationNotFoundException;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;
import uk.org.mygrid.provenance.util.TestUtils;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.ibm.adtech.boca.query.QueryResult;

/**
 * 
 * @author dturi
 * @version $Id: ProvenanceGeneratorTest.java,v 1.1 2005/08/16 12:19:31 turid
 *          Exp $
 */
public class LogBookReporterTests extends TestCase {

	static protected final String WF_INSTANCE = "urn:lsid:net.sf.taverna:wfInstance:";

	static protected final String LAB_1 = "urn:lsid:www.scientific_organisations.org:lab:l5678";

	static protected final String LAB_2 = "urn:lsid:www.scientific_organisations.org:lab:l9999";

	static protected final String TEST_PERSON_1 = "urn:lsid:www.people.org:person:p1234";

	static protected final String TEST_PERSON_2 = "urn:lsid:www.people.org:person:p4321";

	public static String RUN = WF_INSTANCE + "4";

	protected BocaRemoteMetadataService metadataService;

	protected JenaProvenanceOntology ontology = new JenaProvenanceOntology();

	protected OntModel ontModel;

	public static final String PROVENANCE_PREFIX = "PREFIX p: <"
			+ ProvenanceOntologyConstants.NS + "> ";

	public static final String RDF_PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";

	protected static Properties configuration;

	static protected final String BLAST_WORKFLOW_ID = "urn:lsid:www.mygrid.org.uk:operation:"
			+ "16OM6H01JJ0";

	protected static final String WORKFLOW_ID = "urn:lsid:net.sf.taverna:wfDefinition:6";

	static protected final String BLAST_WORKFLOW = "myGrid/blast-rdf.xml";

	static protected final String DILBERT_WORKFLOW = "TomOinn/FetchDailyDilbertComic.xml";

	public static final String USING_RDF = "USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		System.setProperty("raven.profile",
				"file:test-resource/raven-profile.xml");
		System.setProperty("raven.eclipse", "true");
		File tmpDir = File.createTempFile("taverna", "raven");
		assertTrue(tmpDir.delete());
		Repository tempRepository = LocalRepository.getRepository(tmpDir);
		// assertTrue(tmpDir.isDirectory());
		TavernaSPIRegistry.setRepository(tempRepository);
		setLocalProperties();
		TestUtils.setTestProperties();
		metadataService = (BocaRemoteMetadataService) MetadataServiceFactory
				.getInstance(configuration);
		super.setUp();
	}

	protected void setLocalProperties()
			throws LogBookConfigurationNotFoundException {
		configuration = getBocaConfiguration();
		configuration.setProperty(ProvenanceConfigurator.LOGBOOK_LEVEL,
				LogLevel.ALL_STRING);
	}

	protected TupleQueryResult executeQuery(String sparqlQuery)
			throws Exception {
		System.out.println("Testing the following SPARQL query:");
		System.out.println(sparqlQuery);
		QueryResult queryResult = metadataService.query(sparqlQuery);
		assertNotNull("Result is not null", queryResult);
		TupleQueryResult selectResult = queryResult.getSelectResult();
		assertTrue("query result is not empty", selectResult.hasNext());
		return selectResult;
	}

	protected TupleQueryResult executeQuery(String sparqlQuery,
			String namedGraph) throws Exception {
		System.out.println("Testing the following SPARQL query:");
		System.out.println(sparqlQuery);
		QueryResult queryResult = metadataService.queryNamedGraph(sparqlQuery,
				namedGraph);
		assertNotNull("Result is not null", queryResult);
		TupleQueryResult selectResult = queryResult.getSelectResult();
		assertTrue("query result is not empty", selectResult.hasNext());
		return selectResult;
	}

	// public void query(String query) throws Exception {
	// System.out.println(query);
	// JenaMetadataService rdfRepository = new JenaMetadataService(
	// configuration);
	// Iterator iterator = rdfRepository.query(query);
	// while (iterator.hasNext()) {
	// Map nextMap = (Map) iterator.next();
	// System.out.println(nextMap);
	// }
	// }

	public void execute(String workflowLocation) throws Exception {
		UserContext userContext = new SimpleUserContext(
				ProvenanceConfigurator.DEFAULT_EXPERIMENTER,
				ProvenanceConfigurator.DEFAULT_ORGANIZATION,
				"urn:lsid:removeme", "zapp", "angelica");
		new WorkflowEnactorHelper(configuration).executeWorkflow(
				workflowLocation, userContext);
	}

	public void execute(String workflowLocation, String workflowInputsLocation)
			throws Exception {
		SAXBuilder builder = new SAXBuilder();
		String inputFilePath = ClassLoader.getSystemResource(
				workflowInputsLocation).getFile();
		Document inputDoc = builder.build(new FileReader(inputFilePath));
		Map<String, DataThing> inputs = DataThingXMLFactory
				.parseDataDocument(inputDoc);
		UserContext userContext = new SimpleUserContext(
				ProvenanceConfigurator.DEFAULT_EXPERIMENTER,
				ProvenanceConfigurator.DEFAULT_ORGANIZATION,
				"urn:lsid:removeme", "zapp", "angelica");
		new WorkflowEnactorHelper(configuration).executeWorkflow(
				workflowLocation, inputs, userContext);
	}

	public Set<String> getProcessRuns(String workflowRun)
			throws MetadataServiceException {
		Set<String> processRuns = new HashSet<String>(metadataService
				.getProcessesRuns(workflowRun));
		return processRuns;
	}

	public Set<String> getProcessRuns() throws MetadataServiceException {
		return getProcessRuns(RUN);
	}

	protected void populateModel(String workflowRun, Set<String> processRuns)
			throws MetadataServiceException {
		ontology.loadSchema();
		ontModel = ontology.getOntModel();
		ontModel.add(metadataService.retrieveGraphModel(workflowRun));
		for (String processRun : processRuns) {
			Model processModel = metadataService.retrieveGraphModel(processRun);
			if (processModel != null)
				ontModel.add(processModel);
			else
				System.out.println("No model for process run = " + processRun);
		}
	}

	public static Properties getBocaConfiguration()
			throws LogBookConfigurationNotFoundException {
		Properties metadataStoreConfiguration = ProvenanceConfigurator
				.getConfiguration();
//		metadataStoreConfiguration.setProperty(
//				ProvenanceConfigurator.KAVE_TYPE_KEY,
//				ProvenanceConfigurator.BOCA);
		return metadataStoreConfiguration;
	}

}
