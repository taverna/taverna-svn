/*
 * Created on 29-Apr-2004
 * 
 * Copyright (C) 2003 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate. Authorship of the modifications
 * may be determined from the ChangeLog placed at the end of this file.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *  
 */
package uk.org.mygrid.logbook.metadataservice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.memory.MemoryStore;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.Ontology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.GraphRemovalException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceCreationException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGenerator;
import uk.org.mygrid.logbook.util.DataProvenance;
import uk.org.mygrid.logbook.util.ProcessRunBean;
import uk.org.mygrid.logbook.util.Utils;
import uk.org.mygrid.logbook.util.WorkflowRunBean;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.ibm.adtech.boca.client.DatasetService;
import com.ibm.adtech.boca.common.exceptions.BocaException;
import com.ibm.adtech.boca.model.Constants;
import com.ibm.adtech.boca.model.INamedGraph;
import com.ibm.adtech.boca.model.repository.RepositoryConectionProperties;
import com.ibm.adtech.boca.model.repository.RepositoryProperties;
import com.ibm.adtech.boca.query.QueryResult;

/**
 * Boca named RDF graphs repository.
 * 
 * @author dturi
 * 
 */
public class BocaRemoteMetadataService implements MetadataService {

	public static final String RDF_PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";

	static Logger logger = Logger.getLogger(BocaRemoteMetadataService.class);

	private Properties configuration;

	private DatasetService datasetService;

	private INamedGraph remoteGraph = null;

	public Properties getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Properties configuration) {
		this.configuration = configuration;
	}

	public BocaRemoteMetadataService() {
		// Default constructor
	}

	/**
	 * Connects to the repository specified in <code>properties</code>.
	 * 
	 * @param configuration
	 *            {@link Properties}to connect.
	 * @throws MetadataServiceCreationException
	 */
	public BocaRemoteMetadataService(Properties configuration)
			throws MetadataServiceCreationException {
		this.configuration = configuration;
		initialise();
	}

	public void initialise() throws MetadataServiceCreationException {
		try {
			InputStream inputStream = BocaRemoteMetadataService.class
					.getClassLoader().getResourceAsStream("boca.properties");
			configuration.load(inputStream);
			setBocaDatabaseProperties();
			datasetService = new DatasetService(configuration);
		} catch (IOException e) {
			throw new MetadataServiceCreationException(e);
		}
		// try {
		/*
		 * Setting replication mode immediate does not work for the UI, at least
		 * for Derby.
		 * 
		 * datasetService.getDatasetReplicator().setReplicationMode(
		 * IReplicationService.REPLICATION_IMMEDIATE);
		 */
		// createGraphsMetadataGraph();
		// } catch (BocaException e) {
		// logger.error(e);
		// throw new MetadataServiceCreationException(e);
		// }
	}

	/*
	 * FIXME: change property names to remove MYSQL.
	 */
	private void setBocaDatabaseProperties() {
		configuration
				.setProperty(
						RepositoryConectionProperties.KEY_DATABASE_URL,
						configuration
								.getProperty(ProvenanceConfigurator.METADATA_MYSQL_CONNECTION_URL));
		configuration
				.setProperty(
						RepositoryConectionProperties.KEY_DATABASE_USER,
						configuration
								.getProperty(ProvenanceConfigurator.METADATA_MYSQL_USER));
		configuration
				.setProperty(
						RepositoryConectionProperties.KEY_DATABASE_PASSWORD,
						configuration
								.getProperty(ProvenanceConfigurator.METADATA_MYSQL_PASSWORD));
		configuration.setProperty(
				RepositoryConectionProperties.KEY_DATABASE_TYPE, configuration
						.getProperty(ProvenanceConfigurator.KAVE_TYPE_KEY));
	}

	public void close() {
		if (datasetService != null) {
			datasetService.close();
		}
	}

	public void restart() throws MetadataServiceCreationException {
		close();
		initialise();
	}

	public void clear() throws MetadataServiceException {
		StatementCollector sc = new StatementCollector();
		try {
			String resource = configuration
					.getProperty(RepositoryProperties.KEY_REPOSITORY_INITFILE);
			resource = resource.split(":")[1]; // property is resource:/...
			RDFParser parser = Rio.createParser(RDFFormat
					.forFileName("initializeNew.nt"));
			InputStream initializationStream;
			initializationStream = BocaRemoteMetadataService.class
					.getResourceAsStream(resource);
			parser.setRDFHandler(sc);
			parser.parse(initializationStream, "");
			datasetService.getModelService().reset(sc.getStatements());
		} catch (FileNotFoundException e) {
			throw new MetadataServiceException(e);
		} catch (RDFParseException e) {
			throw new MetadataServiceException(e);
		} catch (RDFHandlerException e) {
			throw new MetadataServiceException(e);
		} catch (IOException e) {
			throw new MetadataServiceException(e);
		} catch (BocaException e) {
			throw new MetadataServiceException(e);
		}
	}

	void createGraphsMetadataGraph() throws BocaException {
		// FIXME this should be done automatically by Boca via login
		URI namedGraphURI = datasetService.getValueFactory().createURI(
				GRAPHS_METADATA);
		boolean createIfNecessary = true;
		try {
			remoteGraph = datasetService.getRemoteGraph(namedGraphURI,
					createIfNecessary);
			URI graphsMetadata = datasetService.getValueFactory().createURI(
					GRAPHS_METADATA);
			URI creatorProperty = datasetService.getValueFactory().createURI(
					CREATOR_PROPERTY.getURI());
			URI creator = datasetService.getValueFactory().createURI(
					getCreator());
			URI dateProperty = datasetService.getValueFactory().createURI(
					DATE_PROPERTY.getURI());
			// all operations to Boca models occur in transactions. If any
			// operations are
			// applied outside of a begin/commit, each operation is assigned
			// it's own
			// transaction. Thus, it is recommended that begin/commit subsume as
			// many operations
			// as possible.
			datasetService.begin();
			try {
				// do whatever you want to the model, read write,etc..
				remoteGraph.add(graphsMetadata, creatorProperty, creator);
				remoteGraph.add(graphsMetadata, dateProperty, JenaSesameHelper
						.getCurrentTimeLiteral());
				datasetService.commit();
			} catch (Exception e) {
				datasetService.abort();
				logger.error(e);
			}

			// Push all transaction to the server synchronously. Even before
			// this replication
			// occurs, all models created with the given DatasetService will
			// reflect the
			// committed transactions.
			datasetService.getDatasetReplicator().replicate(true);
		} finally {
			if (remoteGraph != null)
				remoteGraph.close();
		}
	}

	public String getCreator() {
		String creator = configuration.getProperty(
				MetadataService.GRAPH_CREATOR, MetadataService.ANONYMOUS_USER);
		return creator;
	}

	public void setCreator(String creator) {
		configuration.setProperty(MetadataService.GRAPH_CREATOR, creator);
	}

	// public String storeRDFStringAtNewName(String rdf) {
	// if (logger.isDebugEnabled()) {
	// logger.debug("storeRDFStringAtNewName(String rdf = " + rdf
	// + ") - start");
	// }
	//
	// String graphName = idGenerator.getNextID();
	// storeRDFString(rdf, graphName);
	//
	// if (logger.isDebugEnabled()) {
	// logger.debug("storeRDFStringAtNewName(String) - end");
	// }
	// return graphName;
	// }
	//
	// public void storeQuad(String graphName, String subject, String predicate,
	// String object) {
	// Node graphNode = ResourceFactory.createResource(graphName).asNode();
	// Node subjectNode = ResourceFactory.createResource(subject).asNode();
	// Node predicateNode = ResourceFactory.createProperty(predicate)
	// .asNode();
	// Node objectNode = ResourceFactory.createResource(object).asNode();
	// getGraphSet().addQuad(
	// new Quad(graphNode, new Triple(subjectNode, predicateNode,
	// objectNode)));
	// }
	//
	// public void storeRDFString(String rdf, String graphName) {
	// if (logger.isDebugEnabled()) {
	// logger.debug("storeRDFString(String rdf = " + rdf
	// + ", String graphName = " + graphName + ") - start");
	// }
	//
	// Model tempModel = toJenaModel(rdf);
	// storeModel(tempModel, graphName);
	//
	// if (logger.isDebugEnabled()) {
	// logger.debug("storeRDFString(String, String) - end");
	// }
	// }
	//
	// public void storeRDFFile(String rdfFile, String graphName) {
	// if (logger.isDebugEnabled()) {
	// logger.debug("storeRDFFile(String rdfFile = " + rdfFile
	// + ", String graphName = " + graphName + ") - start");
	// }
	//
	// InputStream in = FileManager.get().open(rdfFile);
	// if (in == null) {
	// throw new IllegalArgumentException("File: " + rdfFile
	// + " not found");
	// }
	// Model model = ModelFactory.createDefaultModel();
	// model.read(in, "");
	// storeModel(model, graphName);
	//
	// if (logger.isDebugEnabled()) {
	// logger.debug("storeRDFFile(String, String) - end");
	// }
	// }

	public void updateInstanceData(Ontology ontology, String graphName)
			throws MetadataServiceException {
		String instanceData = ontology.getInstanceDataAsString();
		logger.debug("Updating graph " + graphName + " with " + instanceData);
		updateRDFGraph(instanceData, graphName);
	}

	public void updateRDFGraph(String instanceData, String graphName)
			throws MetadataServiceException {
		updateRDFGraph(instanceData, graphName,
				ProvenanceOntology.PROVENANCE_NS);
	}

	public synchronized void updateRDFGraph(String instanceData,
			String graphName, String namespace) throws MetadataServiceException {
		if (logger.isDebugEnabled()) {
			logger.debug("updateRDFGraph(String instanceData=" + instanceData
					+ ", String graphName=" + graphName + ", String namespace="
					+ namespace + ") - start");
		}
		Repository repository;
		try {
			repository = JenaSesameHelper.toRepository(instanceData, namespace);
		} catch (RepositoryException e) {
			logger.error(e);
			throw new MetadataServiceException(e);
		} catch (RDFParseException e) {
			logger.error(e);
			throw new MetadataServiceException(e);
		} catch (IOException e) {
			logger.error(e);
			throw new MetadataServiceException(e);
		}
		storeRepository(repository, graphName);
		if (logger.isDebugEnabled()) {
			logger.debug("updateRDFGraph(String, String, String) - end");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.NamedRDFGraphsPersister#storeInstanceData(uk.ac.man.cs.img.mygrid.provenance.knowledge.Ontology,
	 *      java.lang.String)
	 */
	public void storeInstanceData(Ontology ontology, String graphName)
			throws MetadataServiceException {
		String instanceData = ontology.getInstanceDataAsString();
		storeRDFGraph(instanceData, graphName);
	}

	public synchronized void addQuad(String graphName, String subject,
			String predicate, String object) throws MetadataServiceException {
		boolean createIfNecessary = true;
		try {
			remoteGraph = datasetService.getRemoteGraph(datasetService
					.getValueFactory().createURI(graphName), createIfNecessary);
			URI subjectResource = datasetService.getValueFactory().createURI(
					subject);
			URI property = datasetService.getValueFactory()
					.createURI(predicate);
			URI objectResource = datasetService.getValueFactory().createURI(
					object);
			datasetService.begin();
			try {
				remoteGraph.add(subjectResource, property, objectResource);
				datasetService.commit();
			} catch (Exception e) {
				datasetService.abort();
				logger.error(e);
			}
			datasetService.getDatasetReplicator().replicate(true);
		} catch (BocaException e) {
			logger.error(e);
			throw new MetadataServiceException(e);
		} finally {
			if (remoteGraph != null)
				remoteGraph.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.NamedRDFGraphsPersister#storeModel(com.hp.hpl.jena.rdf.model.Model,
	 *      java.lang.String)
	 */
	public synchronized void storeModel(Model model, String graphName)
			throws MetadataServiceException {
		if (logger.isDebugEnabled()) {
			logger.debug("storeModel(Model model = " + model
					+ ", String graphName = " + graphName + ") - start");
		}
		Repository repository;
		try {
			repository = JenaSesameHelper.jenaToRepository(model);
		} catch (RepositoryException e) {
			logger.error(e);
			throw new MetadataServiceException(e);
		} catch (RDFParseException e) {
			logger.error(e);
			throw new MetadataServiceException(e);
		} catch (IOException e) {
			logger.error(e);
			throw new MetadataServiceException(e);
		}
		storeRepository(repository, graphName);

		if (logger.isDebugEnabled()) {
			logger.debug("storeModel(Model, String) - end");
		}
	}

	private void storeRepository(Repository repository, String graphName)
			throws MetadataServiceException {
		try {
			remoteGraph = datasetService.getRemoteGraph(datasetService
					.getValueFactory().createURI(graphName), true);
			datasetService.begin();
			try {
				RepositoryConnection connection = repository.getConnection();
				RepositoryResult<Statement> statements = connection
						.getStatements((Resource) null, null, null, false,
								new Resource[] {});
				while (statements.hasNext())
					remoteGraph.add(statements.next());
				statements.close();
				connection.close();
				datasetService.commit();
			} catch (Exception e) {
				datasetService.abort();
				logger.error(e);
			}
			datasetService.getDatasetReplicator().replicate(true);
		} catch (BocaException e) {
			logger.error(e);
			throw new MetadataServiceException(e);
		} finally {
			if (remoteGraph != null)
				remoteGraph.close();
		}
	}

	public Repository toRepository() throws RepositoryException,
			RDFParseException, IOException {
		Repository myRepository = new SailRepository(new MemoryStore());
		myRepository.initialize();
		RepositoryConnection con = myRepository.getConnection();
		URI uri1 = datasetService.getValueFactory().createURI("urn:test");
		URI uri2 = datasetService.getValueFactory().createURI("urn:test2");
		URI uri3 = datasetService.getValueFactory().createURI("urn:test3");
		con.add(uri1, uri2, uri3, new Resource[] {});
		con.close();
		return myRepository;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.NamedRDFGraphsPersister#storeRDFGraph(java.lang.String,
	 *      java.lang.String)
	 */
	public void storeRDFGraph(String rdfGraph, String graphName)
			throws MetadataServiceException {
		storeRDFGraph(rdfGraph, graphName, ProvenanceOntology.PROVENANCE_NS);
	}

	public void storeRDFGraph(String rdfGraph, String graphName,
			String namespace) throws MetadataServiceException {
		try {
			Repository repository = JenaSesameHelper.toRepository(rdfGraph,
					namespace);
			storeRepository(repository, graphName);
		} catch (RepositoryException e) {
			throw new MetadataServiceException(e);
		} catch (RDFParseException e) {
			throw new MetadataServiceException(e);
		} catch (IOException e) {
			throw new MetadataServiceException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.NamedRDFGraphsPersister#storeModel(java.net.URL,
	 *      java.lang.String)
	 */
	public void storeModel(URL modelURL, String graphName) throws IOException,
			MetadataServiceException {
		if (logger.isDebugEnabled()) {
			logger.debug("storeModel(URL modelURL = " + modelURL
					+ ", String graphName = " + graphName + ") - start");
		}

		storeModel(modelURL, graphName, ProvenanceOntology.PROVENANCE_NS);

		if (logger.isDebugEnabled()) {
			logger.debug("storeModel(URL, String) - end");
		}
	}

	public void storeModel(URL modelURL, String graphName, String namespace)
			throws IOException, MetadataServiceException {
		try {
			Repository repository = JenaSesameHelper.toRepository(modelURL,
					namespace);
			storeRepository(repository, graphName);
		} catch (RepositoryException e) {
			throw new MetadataServiceException(e);
		} catch (RDFParseException e) {
			throw new MetadataServiceException(e);
		}
	}

	/**
	 * Adds creator and creation date metadata to <code>graphName</code>.
	 * 
	 * @param graphName
	 *            the name of the stored {@link com.hp.hpl.jena.graph.Graph}.
	 */
	synchronized void storeGraphMetadata(String graphName) {
		// FIXME
		// if (logger.isDebugEnabled()) {
		// logger.debug("storeGraphMetadata(String graphName = " + graphName
		// + ") - start");
		// }
		//
		// NamedGraph graphsmetadata = getGraphsMetadataGraph();
		// Node graphNameNode = Node.create(graphName);
		// Node creator = null; // getCreatorNode();
		// graphsmetadata.add(new Triple(graphNameNode,
		// MetadataService.CREATOR_PROPERTY.asNode(), creator));
		// Node time = getCurrentTimeLiteral();
		// graphsmetadata
		// .add(new Triple(graphNameNode, MetadataService.DATE_PROPERTY
		// .asNode(), getCurrentTimeLiteral()));
		// logger.debug("Graph: " + graphName);
		// logger.debug("Creator: " + creator.getURI());
		// logger.debug("Time: " + time);
	}

	/**
	 * Retrieves the graph corresponding to <code>graphName</code> and returns
	 * it as a String containing its RDF/XML representation.
	 * 
	 * @param graphName
	 *            a String.
	 * @return a String containing an RDF/XML representation of the graph.
	 * @throws MetadataServiceException
	 */
	public String retrieveGraph(String graphName)
			throws MetadataServiceException {
		Model model = retrieveGraphModel(graphName);
		String result = null;
		if (model != null) {
			StringWriter writer = new StringWriter();
			model.write(writer);
			result = writer.toString();
		}
		return result;
	}

	/**
	 * Returns a {@link Model}corresponding to <code>graphName</code>.
	 * 
	 * @param graphName
	 *            a String.
	 * @return a {@link Model}.
	 * @throws MetadataServiceException
	 */
	public Model retrieveGraphModel(String graphName)
			throws MetadataServiceException {
		if (logger.isDebugEnabled()) {
			logger.debug("retrieveGraphModel(String graphName = " + graphName
					+ ") - start");
		}

		INamedGraph namedGraph = null;
		try {
			namedGraph = datasetService.getRemoteGraph(datasetService
					.getValueFactory().createURI(graphName), false);
		} catch (BocaException e) {
			logger.error(e);
			throw new MetadataServiceException(e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("retrieveGraphModel(String) - Model model="
					+ namedGraph);
			logger.debug("retrieveGraphModel(String) - end");
		}

		Model model = JenaSesameHelper.toJenaModel(namedGraph);
		// WARNING: this model should be closed after use to avoid memory leaks.
		return model;
	}

	public Model retrieveGraphModel(String graphName, String namespace)
			throws MetadataServiceException {
		return retrieveGraphModel(graphName);
	}

	/**
	 * Returns an array of all names of graphs created by <code>creator</code>;
	 * 
	 * @param creator
	 *            a String identifying the creator.
	 * @return an array of Strings
	 */
	public String[] retrieveGraphsCreatedBy(String creator) {
		// FIXME
		List graphNames = new ArrayList();
		// String triql = "SELECT ?graph " + "WHERE "
		// + bracketify(MetadataService.GRAPHS_METADATA) + " ( ?graph "
		// + bracketify(MetadataService.CREATOR_PROPERTY.getURI()) + " "
		// + bracketify(creator) + ")";
		// logger.debug(triql);
		// Iterator iterator = TriQLQuery.exec(getGraphSet(), triql);
		// while (iterator.hasNext()) {
		// Map oneResult = (Map) iterator.next();
		// Node graph = (Node) oneResult.get("graph");
		// graphNames.add(graph.getURI().toString());
		// }
		String[] graphArray = toStringArray(graphNames);
		return graphArray;
	}

	private String[] toStringArray(List graphNames) {
		// FIXME
		String[] graphArray = new String[graphNames.size()];
		Iterator iter = graphNames.iterator();
		for (int i = 0; i < graphArray.length; i++)
			graphArray[i] = ((String) iter.next());
		return graphArray;
	}

	/**
	 * Removes the graph <code>graphName</code> and also all the quads
	 * referring to it in {@link MetadataService#GRAPHS_METADATA}.
	 * 
	 * @param graphName
	 *            the name of the {@link com.hp.hpl.jena.graph.Graph}to be
	 *            removed.
	 * @throws MetadataServiceException
	 */
	public void removeGraph(String graphName) throws GraphRemovalException {
		if (logger.isDebugEnabled()) {
			logger.debug("removeGraph(String graphName = " + graphName
					+ ") - start");
		}
		try {
			remoteGraph = datasetService.getRemoteGraph(datasetService
					.getValueFactory().createURI(graphName), false);
			remoteGraph.clear();
		} catch (BocaException e) {
			logger.error(e);
			throw new GraphRemovalException(e);
		} finally {
			if (remoteGraph != null)
				remoteGraph.close();
		}

		if (logger.isDebugEnabled()) {
			logger.debug("removeGraph(String) - end");
		}
	}

	/**
	 * Removes the graph corresponding to <code>workflowRun</code> from the
	 * repository and then does the same by recursion for all its possible
	 * nested workflow runs.
	 * 
	 * @param workflowRun
	 * @throws MetadataServiceException
	 */
	public void removeWorkflowRun(String workflowRun)
			throws MetadataServiceException {
		List<String> nestedRuns = getNestedRuns(workflowRun);
		for (String nestedRun : nestedRuns) {
			removeWorkflowRun(nestedRun);
		}
		removeGraph(workflowRun);
	}

	/**
	 * Gets the names of the nested workflow runs for <code>workflowRun</code>.
	 * 
	 * @param workflowRun
	 * @return a List of Strings
	 * @throws MetadataServiceException
	 */
	public List<String> getNestedRuns(String workflowRun)
			throws MetadataServiceException {
		return getObjectPropertyValues(workflowRun,
				ProvenanceOntologyConstants.ObjectProperties.NESTEDRUN);
	}

	/**
	 * Any of the arguments can be <code>null</code>.
	 * 
	 * @param graph
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return
	 */
	public String[] retrieveStatementsMatchingPattern(String graph,
			String subject, String predicate, String object) {
		if (logger.isDebugEnabled()) {
			logger.debug("retrieveStatementsMatchingPattern(String graph = "
					+ graph + ", String subject = " + subject
					+ ", String predicate = " + predicate
					+ ", String object = " + object + ") - start");
		}

		// FIXME
		// Node graphNode = Node.ANY;
		// Node subjectNode = Node.ANY;
		// Node propertyNode = Node.ANY;
		// Node objectNode = Node.ANY;
		//
		// if (graph != null)
		// graphNode = ResourceFactory.createResource(graph).asNode();
		// if (subject != null)
		// subjectNode = ResourceFactory.createResource(subject).asNode();
		// if (predicate != null)
		// propertyNode = ResourceFactory.createProperty(predicate).asNode();
		// if (object != null)
		// objectNode = ResourceFactory.createResource(object).asNode();
		//
		// Iterator iterator = getGraphSet().findQuads(graphNode, subjectNode,
		// propertyNode, objectNode);
		// List quads = new ArrayList();
		// while (iterator.hasNext()) {
		// Quad quad = (Quad) iterator.next();
		// quads.add(quad.toString());
		// }
		// String[] returnStringArray = toStringArray(quads);
		// if (logger.isDebugEnabled()) {
		// logger
		// .debug("retrieveStatementsMatchingPattern(String, String, String,
		// String) - end");
		// }
		// return returnStringArray;
		return null;
	}

	/**
	 * Executes <code>query</code>.
	 * 
	 * @param sparqlQuery
	 *            a SPARQL query String.
	 * @return the {@link Graph} resulting from executing
	 *         <code>sparqlQuery</code>.
	 * @throws MetadataServiceException
	 */
	public QueryResult query(String sparqlQuery, Set<URI> defaultGraphs,
			Set<URI> namedGraphs) throws MetadataServiceException {
		try {
			QueryResult graph = datasetService.getModelService().execQuery(
					defaultGraphs, namedGraphs, sparqlQuery,
					Syntax.syntaxSPARQL.getSymbol());
			return graph;
		} catch (BocaException e) {
			logger.error(e);
			throw new MetadataServiceException(e);
		}
	}

	/**
	 * Executes <code>query</code>.
	 * 
	 * @param sparqlQuery
	 *            a SPARQL query String.
	 * @return the {@link Graph} resulting from executing
	 *         <code>sparqlQuery</code>.
	 * @throws MetadataServiceException
	 */
	public QueryResult query(String sparqlQuery, Set<URI> defaultGraphs)
			throws MetadataServiceException {
		QueryResult graph = query(sparqlQuery, defaultGraphs,
				new HashSet<URI>());
		return graph;
	}

	public QueryResult queryNamedGraph(String sparqlQuery, String namedGraph)
			throws MetadataServiceException {
		HashSet<URI> namedGraphs = new HashSet<URI>();
		namedGraphs.add(datasetService.getValueFactory().createURI(namedGraph));
		return query(sparqlQuery, new HashSet<URI>(), namedGraphs);
	}

	/**
	 * Executes <code>query</code>.
	 * 
	 * @param sparqlQuery
	 *            a SPARQL query String.
	 * @return the {@link Graph} resulting from executing
	 *         <code>sparqlQuery</code>.
	 * @throws MetadataServiceException
	 */
	public QueryResult query(String sparqlQuery)
			throws MetadataServiceException {
		HashSet<URI> defaultGraphs = new HashSet<URI>();
		defaultGraphs.add(datasetService.getValueFactory().createURI(
				Constants.allNamedGraphsUri));
		QueryResult graph = query(sparqlQuery, defaultGraphs);
		return graph;
	}

	public boolean ask(String sparqlAskQuery, Set<URI> defaultGraphs,
			Set<URI> namedGraphs) throws MetadataServiceException {
		boolean result;
		QueryResult queryResult = null;
		try {
			queryResult = datasetService.getModelService().execQuery(
					defaultGraphs, namedGraphs, sparqlAskQuery,
					Syntax.syntaxSPARQL.getSymbol());
			if (!queryResult.isAskResult()) {
				throw new MetadataServiceException("Query " + sparqlAskQuery
						+ " is not an ASK query.");
			}
			result = queryResult.getAskResult();
			return result;
		} catch (BocaException e) {
			throw new MetadataServiceException(e);
		}
	}

	public boolean ask(String sparqlAskQuery, Set<URI> defaultGraphs)
			throws MetadataServiceException {
		return ask(sparqlAskQuery, defaultGraphs, new HashSet<URI>());
	}

	public boolean ask(String sparqlAskQuery) throws MetadataServiceException {
		HashSet<URI> defaultGraphs = new HashSet<URI>();
		defaultGraphs.add(datasetService.getValueFactory().createURI(
				Constants.allNamedGraphsUri));
		return ask(sparqlAskQuery, defaultGraphs);
	}

	/**
	 * Gets all the (ids of ) process runs executed by
	 * <code>workflowRunLSID</code>
	 * 
	 * @param workflowRunLSID
	 * @return List of Strings
	 * @throws MetadataServiceException
	 */
	public List<String> getProcessesRuns(String workflowRunLSID)
			throws MetadataServiceException {
		return getFromRun(
				workflowRunLSID,
				"processRun",
				JenaProvenanceOntology
						.bracketify(ProvenanceOntologyConstants.ObjectProperties.EXECUTEDPROCESSRUN));
	}

	/**
	 * Maps workflow runs ids to their process runs.
	 * 
	 * @return a Map from Strings to Sets of Strings
	 * @throws MetadataServiceException
	 */
	public Map<String, Set<String>> getAllProcessesRuns()
			throws MetadataServiceException {
		Map<String, Set<String>> processRuns = new HashMap<String, Set<String>>();
		TupleQueryResult results = null;
		try {
			String query = "SELECT * " + "WHERE { " + "?workflowRunId <"
					+ ProvenanceVocab.EXECUTED_PROCESS_RUN.getURI()
					+ "> ?processRunId }";
			QueryResult result = query(query);
			results = result.getSelectResult();
			while (results.hasNext()) {
				BindingSet sol = results.next();
				String workflowRunId = sol.getValue("workflowRunId").toString();
				String processRunId = sol.getValue("processRunId").toString();
				Set<String> runs = processRuns.get(workflowRunId);
				if (runs == null) {
					runs = new HashSet<String>();
					processRuns.put(workflowRunId, runs);
				}
				runs.add(processRunId);
			}
		} catch (QueryEvaluationException e) {
			throw new MetadataServiceException(e);
		} finally {
			if (results != null)
				try {
					results.close();
				} catch (QueryEvaluationException e) {
					throw new MetadataServiceException(e);
				}
		}
		return processRuns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService#getWorkflowInputs(java.lang.String)
	 */
	public List<String> getWorkflowInputs(String lsid)
			throws MetadataServiceException {
		return getFromRun(
				lsid,
				"workflowInput",
				JenaProvenanceOntology
						.bracketify(ProvenanceOntologyConstants.ObjectProperties.WORKFLOWINPUT));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService#getWorkflowOutputs(java.lang.String)
	 */
	public List<String> getWorkflowOutputs(String workflowRunLSID)
			throws MetadataServiceException {
		return getFromRun(
				workflowRunLSID,
				"workflowOutput",
				JenaProvenanceOntology
						.bracketify(ProvenanceOntologyConstants.ObjectProperties.WORKFLOWOUTPUT));
	}

	public List<String> getProcessInputs(String processID)
			throws MetadataServiceException {
		List<String> inputs = getObjectPropertyValues(processID,
				ProvenanceOntologyConstants.ObjectProperties.PROCESSINPUT);
		return inputs;
	}

	public List<String> getProcessOutputs(String processID)
			throws MetadataServiceException {
		List<String> outputs = getObjectPropertyValues(processID,
				ProvenanceOntologyConstants.ObjectProperties.PROCESSOUTPUT);
		return outputs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService#getPropertyValue(java.lang.String,
	 *      java.lang.String)
	 */
	public String getFirstObjectPropertyValue(String sourceIndividual,
			String objectProperty) throws MetadataServiceException {
		TupleQueryResult results = null;
		try {
			String query = "SELECT ?value " + "WHERE { <" + sourceIndividual
					+ "> <" + objectProperty + "> ?value } ";
			QueryResult graph = query(query);
			results = graph.getSelectResult();
			try {
				if (!results.hasNext())
					return null;
				BindingSet sol = results.next();
				Value value = sol.getValue("value");
				if (!(value instanceof URI))
					throw new MetadataServiceException("The first value "
							+ value + " is not a URI resource.");
				return value.toString();
			} catch (QueryEvaluationException e) {
				logger.error(e);
				throw new MetadataServiceException(e);
			}
		} finally {
			if (results != null)
				try {
					results.close();
				} catch (QueryEvaluationException e) {
					logger.error(e);
					throw new MetadataServiceException(e);
				}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService#getObjectPropertyValues(java.lang.String,
	 *      java.lang.String)
	 */
	public List<String> getObjectPropertyValues(String sourceIndividual,
			String objectProperty) throws MetadataServiceException {
		List<String> values = new ArrayList<String>();
		TupleQueryResult results = null;
		try {
			String query = "SELECT ?value " + "WHERE { <" + sourceIndividual
					+ "> <" + objectProperty + "> ?value } ";
			QueryResult graph = query(query);
			results = graph.getSelectResult();
			try {
				while (results.hasNext()) {
					BindingSet sol = results.next();
					Value value = sol.getValue("value");
					if (value instanceof URI) {
						String uri = ((URI) value).toString();
						values.add(uri);
					}
				}
				return values;
			} catch (QueryEvaluationException e) {
				logger.error(e);
				throw new MetadataServiceException(e);
			}
		} finally {
			if (results != null)
				try {
					results.close();
				} catch (QueryEvaluationException e) {
					logger.error(e);
					throw new MetadataServiceException(e);
				}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService#getFirstDatatypePropertyValue(java.lang.String,
	 *      java.lang.String)
	 */
	public String getFirstDatatypePropertyValue(String sourceIndividual,
			String datatypeProperty) throws MetadataServiceException {
		TupleQueryResult results = null;
		try {
			String query = "SELECT ?value " + "WHERE { <" + sourceIndividual
					+ "> <" + datatypeProperty + "> ?value } ";
			QueryResult graph = query(query);
			results = graph.getSelectResult();
			try {
				if (!results.hasNext())
					return null;
				BindingSet sol = results.next();
				Value value = sol.getValue("value");
				if (!(value instanceof Literal))
					throw new MetadataServiceException("The first value "
							+ value + " is not a literal.");
				String label = ((Literal) value).getLabel();
				return label;
			} catch (QueryEvaluationException e) {
				logger.error(e);
				throw new MetadataServiceException(e);
			}
		} finally {
			if (results != null)
				try {
					results.close();
				} catch (QueryEvaluationException e) {
					logger.error(e);
					throw new MetadataServiceException(e);
				}
		}
	}

	public List<String> getDatatypePropertyValues(String sourceIndividual,
			String datatypeProperty) throws MetadataServiceException {
		List<String> values = new ArrayList<String>();
		TupleQueryResult results = null;
		try {
			String query = "SELECT ?value " + "WHERE { <" + sourceIndividual
					+ "> <" + datatypeProperty + "> ?value } ";
			QueryResult graph = query(query);
			results = graph.getSelectResult();
			try {
				while (results.hasNext()) {
					BindingSet sol = results.next();
					Value value = sol.getValue("value");
					if (value instanceof Literal)
						values.add(((Literal) value).getLabel());
				}
				return values;
			} catch (QueryEvaluationException e) {
				logger.error(e);
				throw new MetadataServiceException(e);
			}
		} finally {
			if (results != null)
				try {
					results.close();
				} catch (QueryEvaluationException e) {
					logger.error(e);
					throw new MetadataServiceException(e);
				}
		}
	}

	public String getUnparsedWorkflowStartDate(String workflowRun)
			throws MetadataServiceException {
		String value = getFirstDatatypePropertyValue(workflowRun,
				ProvenanceVocab.START_TIME.getURI());
		value = value.substring(1, value.lastIndexOf("\""));
		return value;
	}

	public String getUnparsedProcessEndDate(String processRun)
			throws MetadataServiceException {
		String value = getFirstDatatypePropertyValue(processRun,
				ProvenanceOntologyConstants.DatatypeProperties.ENDTIME);
		if (value == null)
			throw new MetadataServiceException("No end time found for "
					+ processRun);
		value = value.substring(1, value.lastIndexOf("\""));
		return value;
	}

	public boolean isProcessIteration(String processURI)
			throws MetadataServiceException {
		return isIndividualOfType(processURI, ProvenanceVocab.PROCESS_ITERATION
				.getURI());
	}

	public boolean isProcessWithIterations(String processURI)
			throws MetadataServiceException {
		return isIndividualOfType(processURI,
				ProvenanceVocab.PROCESS_RUN_WITH_ITERATIONS.getURI());
	}

	public List<String> getAllProcessRunsWithIterations()
			throws MetadataServiceException {
		return getIndividualsOfType(ProvenanceVocab.PROCESS_RUN_WITH_ITERATIONS
				.getURI());
	}

	public boolean isIndividualOfType(String individual, String type)
			throws MetadataServiceException {
		String query = RDF_PREFIX + "ASK { <" + individual + "> rdf:type <"
				+ type + "> }";
		return ask(query);
	}

	/**
	 * Query to obtain all the workflow LSIDS for the current user as stated by
	 * mygrid.usercontext.experimenter
	 * 
	 * @param experimenter
	 * @return a Vector of the workflow LSIDs as Strings
	 * @throws MetadataServiceException
	 */
	public Vector<String> getUserWorkFlows(String experimenter)
			throws MetadataServiceException {
		Vector<String> usersWorkflows = new Vector<String>();
		final String query = "SELECT ?workflowRun "
				+ "WHERE { ?workflowRun "
				+ JenaProvenanceOntology.bracketify(ProvenanceVocab.LAUNCHED_BY
						.getURI())
				+ JenaProvenanceOntology.bracketify(experimenter) + "} ";
		TupleQueryResult results = null;
		try {
			QueryResult graph = query(query);
			results = graph.getSelectResult();
			try {
				while (results.hasNext()) {
					BindingSet sol = results.next();
					Value workflowRun = sol.getValue("workflowRun");
					if (workflowRun instanceof URI)
						usersWorkflows.add(workflowRun.toString());
				}
				return usersWorkflows;
			} catch (QueryEvaluationException e) {
				logger.error(e);
				throw new MetadataServiceException(e);
			}
		} finally {
			if (results != null)
				try {
					results.close();
				} catch (QueryEvaluationException e) {
					logger.error(e);
					throw new MetadataServiceException(e);
				}
		}
	}

	private List<String> getFromRun(String workflowRunLSID, String variable,
			String property) throws MetadataServiceException {
		List<String> results = new ArrayList<String>();
		String query = "SELECT ?" + variable + " WHERE { GRAPH <"
				+ workflowRunLSID + "> { <" + workflowRunLSID + "> " + property
				+ " ?" + variable + "  } }";
		HashSet<URI> namedGraph = new HashSet<URI>();
		namedGraph.add(datasetService.getValueFactory().createURI(
				workflowRunLSID));
		TupleQueryResult queryResults = null;
		try {
			QueryResult graph = query(query, new HashSet<URI>(), namedGraph);
			queryResults = graph.getSelectResult();
			try {
				while (queryResults.hasNext()) {
					BindingSet sol = queryResults.next();
					Value value = sol.getValue(variable);
					if (value instanceof URI)
						results.add(value.toString());
				}
				return results;
			} catch (QueryEvaluationException e) {
				logger.error(e);
				throw new MetadataServiceException(e);
			}
		} finally {
			if (queryResults != null)
				try {
					queryResults.close();
				} catch (QueryEvaluationException e) {
					logger.error(e);
					throw new MetadataServiceException(e);
				}
		}
	}

	/**
	 * Puts angled brackets around <code>uri</code>.
	 * 
	 * @param uri
	 *            a String.
	 * @return a String.
	 */
	public static String bracketify(String uri) {
		return "<" + uri + ">";
	}

	// public Map inputDataOfFailedProcessesForExperimenter(String experimenter)
	// {
	// final String query = "SELECT ?workflow, ?workflowRun, ?startTime,
	// ?process, ?inputData WHERE "
	// + "?workflowRun ( ?workflowRun "
	// + ProvenanceOntology
	// .bracketify(ProvenanceOntologyConstants.ObjectProperties.LAUNCHEDBY)
	// + ProvenanceOntology.bracketify(experimenter)
	// + " . ?workflowRun "
	// + ProvenanceOntology
	// .bracketify(ProvenanceOntologyConstants.ObjectProperties.RUNSWORKFLOW)
	// + " ?workflow . ?run rdf:type "
	// + ProvenanceOntology
	// .bracketify(ProvenanceOntologyConstants.Classes.FAILEDPROCESSRUN)
	// + " . ?run "
	// + ProvenanceOntology
	// .bracketify(ProvenanceOntologyConstants.ObjectProperties.RUNSPROCESS)
	// + " ?process . ?process "
	// + ProvenanceOntology
	// .bracketify(ProvenanceOntologyConstants.ObjectProperties.PROCESSINPUT)
	// + " ?inputData . ?workflowRun "
	// + ProvenanceOntology
	// .bracketify(ProvenanceOntologyConstants.DatatypeProperties.STARTTIME)
	// + " ?dateTime ) USING rdf FOR
	// <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
	// logger.debug("TriQL query = " + query);
	// Map result = new HashMap();
	// Iterator iterator = TriQLQuery.exec(graphSet, query);
	// Collection dataCollection;
	// while (iterator.hasNext()) {
	// Map nextMap = (Map) iterator.next();
	// Node workflowRun = (Node) nextMap.get("workflowRun");
	// String workflowRunLSID = workflowRun.getURI();
	// logger.debug("workflowRun = " + workflowRunLSID);
	// Node workflow = (Node) nextMap.get("workflow");
	// String workflowID = workflow.getURI();
	// logger.debug("workflow = " + workflowID);
	// Node startTime = (Node) nextMap.get("startTime");
	// String startTimeValue = startTime.getLiteral().toString();
	// logger.debug("startTime = " + startTimeValue);
	// Node process = (Node) nextMap.get("process");
	// String processLSID = process.getURI();
	// logger.debug("process = " + processLSID);
	// Node inputData = (Node) nextMap.get("inputData");
	// String inputDataLSID = inputData.getURI();
	// logger.debug("inputData = " + inputDataLSID);
	// if (result.containsKey(processLSID))
	// dataCollection = (Collection) result.get(processLSID);
	// else {
	// dataCollection = new HashSet();
	// result.put(processLSID, dataCollection);
	// }
	// dataCollection.add(inputDataLSID);
	// }
	// return result;
	// }

	public List<String> getAllNestedRuns() throws MetadataServiceException {
		return getIndividualsOfType(ProvenanceOntologyConstants.Classes.NESTEDWORKFLOWRUN);
	}

	public List<String> getAllWorkflowRuns() throws MetadataServiceException {
		return getIndividualsOfType(ProvenanceOntologyConstants.Classes.WORKFLOWRUN);
	}

	public List<String> getDataCollectionLSIDs(String dataCollectionLSID)
			throws MetadataServiceException {
		List<String> lsids = getObjectPropertyValues(dataCollectionLSID,
				ProvenanceOntologyConstants.ObjectProperties.CONTAINSDATA);
		return lsids;
	}

	public List<String> getDataPortNames(String dataLSID)
			throws MetadataServiceException {
		List<String> names = getInputDataPortNames(dataLSID);
		names.addAll(getOutputDataPortNames(dataLSID));
		return names;
	}

	public Set<String> getDataSyntacticTypes(String dataLSID)
			throws MetadataServiceException {
		Set<String> syntacticTypes = new HashSet<String>();
		List<String> names = getObjectPropertyValues(dataLSID,
				ProvenanceOntologyConstants.ObjectProperties.INPUTDATAHASNAME);
		names
				.addAll(getObjectPropertyValues(
						dataLSID,
						ProvenanceOntologyConstants.ObjectProperties.OUTPUTDATAHASNAME));
		for (String name : names) {
			List<String> types = getDatatypePropertyValues(
					name,
					ProvenanceOntologyConstants.DatatypeProperties.DATASYNTACTICTYPE);
			syntacticTypes.addAll(types);
		}
		return syntacticTypes;
	}

	public Map<String, DataProvenance> getSimilarData(String dataLSID)
			throws MetadataServiceException {
		Map<String, DataProvenance> similarData = new HashMap<String, DataProvenance>();
		String queryString = "SELECT ?output "
				+ "WHERE { "
				+ "<"
				+ dataLSID
				+ "> "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.OUTPUT_DATA_HAS_NAME
								.getURI()) + " ?output }";
		QueryResult result;
		TupleQueryResult results = null;
		try {
			result = query(queryString);
			results = result.getSelectResult();
			String output = results.next().getValue("output").toString();
			if (results != null) {
				try {
					results.close();
				} catch (QueryEvaluationException e) {
					throw new MetadataServiceException(e);
				}
			}
			queryString = "SELECT * "
					+ "WHERE { ?processRun "
					+ JenaProvenanceOntology
							.bracketify(ProvenanceVocab.PROCESS_OUTPUT.getURI())
					+ "?data . ?data "
					+ JenaProvenanceOntology
							.bracketify(ProvenanceVocab.OUTPUT_DATA_HAS_NAME
									.getURI())
					+ " <"
					+ output
					+ "> . ?processRun "
					+ JenaProvenanceOntology
							.bracketify(ProvenanceVocab.END_TIME.getURI())
					+ " ?date . "
					+ " ?workflowRun "
					+ JenaProvenanceOntology
							.bracketify(ProvenanceVocab.EXECUTED_PROCESS_RUN
									.getURI()) + " ?processRun }";
			result = query(queryString);
			results = result.getSelectResult();
			while (results.hasNext()) {
				BindingSet sol = results.next();
				String data = sol.getValue("data").toString();
				if (data.equals(dataLSID))
					continue;
				String workflowRun = sol.getValue("workflowRun").toString();
				Value value = sol.getValue("date");
				if (!(value instanceof Literal))
					throw new MetadataServiceException("Expected date " + value
							+ " is not a literal.");
				String date = ((Literal) value).getLabel();
				Date parseDateLiteral = null;
				try {
					parseDateLiteral = Utils.parseDateLiteral(date);
				} catch (ParseException e) {
					logger.error(e);
				}
				similarData.put(data, new DataProvenance(data, workflowRun,
						parseDateLiteral));
			}
		} catch (QueryEvaluationException e) {
			throw new MetadataServiceException(e);
		} finally {
			if (results != null)
				try {
					results.close();
				} catch (QueryEvaluationException e) {
					throw new MetadataServiceException(e);
				}
		}
		return similarData;
	}

	public List<String> getIndividualsOfType(String type)
			throws MetadataServiceException {
		List<String> values = new ArrayList<String>();
		TupleQueryResult results = null;
		try {
			String query = "SELECT ?value "
					+ "WHERE { ?value <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"
					+ type + "> } ";
			QueryResult graph = query(query);
			results = graph.getSelectResult();
			try {
				while (results.hasNext()) {
					BindingSet sol = results.next();
					Value value = sol.getValue("value");
					if (value instanceof URI)
						values.add(value.toString());
				}
				return values;
			} catch (QueryEvaluationException e) {
				logger.error(e);
				throw new MetadataServiceException(e);
			}
		} finally {
			if (results != null)
				try {
					results.close();
				} catch (QueryEvaluationException e) {
					logger.error(e);
					throw new MetadataServiceException(e);
				}
		}
	}

	public List<String> getInputDataPortNames(String dataLSID)
			throws MetadataServiceException {
		List<String> names = getObjectPropertyValues(dataLSID,
				ProvenanceOntologyConstants.ObjectProperties.INPUTDATAHASNAME);
		List<String> result = new ArrayList<String>();
		for (String name : names) {
			result.add(ProvenanceGenerator.getInputDataFromURN(name));
		}
		return result;
	}

	public List<String> getNonNestedProcessRuns(String workflowRunLSID)
			throws MetadataServiceException {
		List<String> processesRuns = getProcessesRuns(workflowRunLSID);
		List<String> nestedRuns = getIndividualsOfType(ProvenanceOntologyConstants.Classes.NESTEDWORKFLOWPROCESSRUN);
		processesRuns.removeAll(nestedRuns);
		return processesRuns;
	}

	public List<String> getOutputDataPortNames(String dataLSID)
			throws MetadataServiceException {
		List<String> names = getObjectPropertyValues(dataLSID,
				ProvenanceOntologyConstants.ObjectProperties.OUTPUTDATAHASNAME);
		List<String> result = new ArrayList<String>();
		for (String name : names) {
			result.add(ProvenanceGenerator.getOutputDataFromURN(name));
		}
		return result;
	}

	public String getWorkflowRun(String workflowRunId)
			throws MetadataServiceException {
		Model model = getWorkflowRunModel(workflowRunId);
		String graph = JenaSesameHelper.jenaToString(model);
		return graph;
	}

	public Model getWorkflowRunModel(String workflowRunId)
			throws MetadataServiceException {
		Model model = retrieveGraphModel(workflowRunId);
		List<String> processesRunsIds = getNonNestedProcessRuns(workflowRunId);
		for (String processRunId : processesRunsIds) {
			Model processRun = retrieveGraphModel(processRunId);
			model.add(processRun);
		}
		List<String> nestedRunsIds = getNestedRuns(workflowRunId);
		for (String nestedRunId : nestedRunsIds) {
			Model nestedRun = getWorkflowRunModel(nestedRunId);
			model.add(nestedRun);
		}
		return model;
	}

	public Map<String, WorkflowRunBean> getWorkflowRunBeans()
			throws MetadataServiceException {
		Map<String, WorkflowRunBean> workflowRunBeans = new HashMap<String, WorkflowRunBean>();
		Set<String> workflowInitialIds = new HashSet<String>();
		String query = "SELECT * " + "WHERE { " + "?workflowRunId <"
				+ ProvenanceVocab.START_TIME.getURI() + "> ?date "
				+ ". ?workflowRunId <" + ProvenanceVocab.RUNS_WORKFLOW.getURI()
				+ "> ?workflow . ?workflow <"
				+ ProvenanceVocab.WORKFLOW_INITIAL_LSID.getURI()
				+ "> ?workflowInitialId . OPTIONAL { ?workflow   <"
				+ ProvenanceVocab.WORKFLOW_AUTHOR.getURI()
				+ "> ?author } . OPTIONAL { ?workflow  <"
				+ ProvenanceVocab.WORKFLOW_TITLE.getURI()
				+ "> ?title } . OPTIONAL { ?workflow <"
				+ ProvenanceVocab.WORKFLOW_DESCRIPTION.getURI()
				+ "> ?description  } " + " }";
		QueryResult result;
		TupleQueryResult results = null;
		try {
			result = query(query);
			results = result.getSelectResult();
			while (results.hasNext()) {
				BindingSet sol = results.next();
				WorkflowRunBean workflowRunBean = new WorkflowRunBean();
				String workflowRunId = sol.getValue("workflowRunId").toString();
				workflowRunBean.setWorkflowRunId(workflowRunId);
				String date = sol.getValue("date").toString();
				workflowRunBean.setDate(date);
				workflowRunBeans.put(workflowRunId.toString(), workflowRunBean);
				String workflowInitialId = sol.getValue("workflowInitialId")
						.toString();
				workflowRunBean.setWorkflowInitialId(workflowInitialId);
				workflowInitialIds.add(workflowInitialId);
				String workflowId = sol.getValue("workflow").toString();
				workflowRunBean.setWorkflowId(workflowId);
				Value optionalValue = sol.getValue("title");
				if (optionalValue != null) {
					String title = optionalValue.toString();
					workflowRunBean.setTitle(title);
				}
				optionalValue = sol.getValue("author");
				if (optionalValue != null) {
					String author = optionalValue.toString();
					workflowRunBean.setAuthor(author);
				}
				optionalValue = sol.getValue("description");
				if (optionalValue != null) {
					String description = optionalValue.toString();
					workflowRunBean.setDescription(description);
				}
			}
		} catch (QueryEvaluationException e) {
			throw new MetadataServiceException(e);
		} finally {
			if (results != null)
				try {
					results.close();
				} catch (QueryEvaluationException e) {
					throw new MetadataServiceException(e);
				}
		}

		Set<Entry<String, WorkflowRunBean>> entrySet = workflowRunBeans
				.entrySet();
		Map<String, Set<ProcessRunBean>> processRunBeans = getProcessRunBeans();
		for (Entry<String, WorkflowRunBean> entry : entrySet) {
			entry.getValue().setProcessRunBeans(
					processRunBeans.get(entry.getKey()));
		}

		return workflowRunBeans;
	}

	public Map<String, Set<ProcessRunBean>> getProcessRunBeans()
			throws MetadataServiceException {
		Map<String, String> failedProcessRuns = new HashMap<String, String>();
		String query = RDF_PREFIX + "SELECT * "
				+ "WHERE { ?processRunId rdf:type <"
				+ ProvenanceVocab.FAILED_PROCESS_RUN.getURI()
				+ "> . ?processRunId <" + ProvenanceVocab.CAUSE.getURI()
				+ "> ?cause . " + "}";
		QueryResult result;
		TupleQueryResult results = null;
		try {
			result = query(query);
			results = result.getSelectResult();
			while (results.hasNext()) {
				BindingSet sol = results.next();
				String processRunId = sol.getValue("processRunId").toString();
				String cause = sol.getValue("cause").toString();
				failedProcessRuns.put(processRunId, cause);
			}
		} catch (QueryEvaluationException e) {
			throw new MetadataServiceException(e);
		} finally {
			if (results != null)
				try {
					results.close();
				} catch (QueryEvaluationException e) {
					throw new MetadataServiceException(e);
				}
		}

		Map<String, Set<ProcessRunBean>> processRunIterationsBeans = new HashMap<String, Set<ProcessRunBean>>();
		query = "SELECT * " + "WHERE { ?processRunId <"
				+ ProvenanceVocab.ITERATION.getURI()
				+ "> ?iteration . ?iteration <"
				+ ProvenanceVocab.END_TIME.getURI() + "> ?date . ?iteration <"
				+ ProvenanceVocab.RUNS_PROCESS.getURI()
				+ "> ?process . ?process <"
				+ ProvenanceVocab.CLASS_NAME.getURI()
				+ "> ?processClassName . " + "}";
		try {
			result = query(query);
			results = result.getSelectResult();
			while (results.hasNext()) {
				BindingSet sol = results.next();
				String iteration = sol.getValue("iteration").toString();
				String date = sol.getValue("date").toString();
				String processRunId = sol.getValue("processRunId").toString();
				String process = ((URI) sol.getValue("process")).getLocalName();
				String processClassName = sol.getValue("processClassName")
						.toString();
				Set<ProcessRunBean> iterations = processRunIterationsBeans
						.get(processRunId);
				if (iterations == null) {
					iterations = new HashSet<ProcessRunBean>();
					processRunIterationsBeans.put(processRunId, iterations);
				}
				iterations.add(new ProcessRunBean(iteration, processRunId,
						date, process, processClassName));
			}
		} catch (QueryEvaluationException e) {
			throw new MetadataServiceException(e);
		} finally {
			if (results != null)
				try {
					results.close();
				} catch (QueryEvaluationException e) {
					throw new MetadataServiceException(e);
				}
		}

		List<String> subworkflows = getIndividualsOfType(ProvenanceVocab.NESTED_WORKFLOW_PROCESS_RUN
				.getURI());

		Map<String, Set<ProcessRunBean>> workflowRunsToProcessRuns = new HashMap<String, Set<ProcessRunBean>>();
		Map<String, ProcessRunBean> processRunBeans = new HashMap<String, ProcessRunBean>();
		query = "SELECT * " + "WHERE { ?workflowRunId <"
				+ ProvenanceVocab.EXECUTED_PROCESS_RUN.getURI()
				+ "> ?processRunId . ?processRunId <"
				+ ProvenanceVocab.END_TIME.getURI()
				+ "> ?date . ?processRunId <"
				+ ProvenanceVocab.RUNS_PROCESS.getURI()
				+ "> ?process . ?process <"
				+ ProvenanceVocab.CLASS_NAME.getURI()
				+ "> ?processClassName . " + "}";
		try {
			result = query(query);
			results = result.getSelectResult();
			while (results.hasNext()) {
				BindingSet sol = results.next();
				String workflowRunId = sol.getValue("workflowRunId").toString();
				String processRunId = sol.getValue("processRunId").toString();
				String process = ((URI) sol.getValue("process")).getLocalName();
				String processClassName = sol.getValue("processClassName")
						.toString();
				String date = sol.getValue("date").toString();
				ProcessRunBean processRunBean = processRunBeans
						.get(processRunId);
				if (processRunBean == null) {
					processRunBean = new ProcessRunBean(processRunId,
							workflowRunId, date, process, processClassName);
					processRunBean.setWorkflowRunId(workflowRunId);
					processRunBeans.put(processRunId, processRunBean);
				}
				if (processRunIterationsBeans.containsKey(processRunId)) {
					processRunBean
							.setProcessIterations(processRunIterationsBeans
									.get(processRunId));
				}
				if (failedProcessRuns.containsKey(processRunId)) {
					processRunBean.setFailed(true, failedProcessRuns
							.get(processRunId));
				}
				if (subworkflows.contains(processRunId))
					processRunBean.setSubworkflow(true);
				Set<ProcessRunBean> processRuns = workflowRunsToProcessRuns
						.get(workflowRunId);
				if (processRuns == null) {
					processRuns = new HashSet<ProcessRunBean>();
					workflowRunsToProcessRuns.put(workflowRunId, processRuns);
				}
				processRuns.add(processRunBean);
			}
		} catch (QueryEvaluationException e) {
			throw new MetadataServiceException(e);
		} finally {
			if (results != null)
				try {
					results.close();
				} catch (QueryEvaluationException e) {
					throw new MetadataServiceException(e);
				}
		}
		return workflowRunsToProcessRuns;
	}

}