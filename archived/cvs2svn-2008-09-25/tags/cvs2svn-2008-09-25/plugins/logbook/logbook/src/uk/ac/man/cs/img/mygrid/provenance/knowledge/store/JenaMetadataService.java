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
package uk.ac.man.cs.img.mygrid.provenance.knowledge.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
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

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.Ontology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGenerator;
import uk.org.mygrid.logbook.util.DataProvenance;
import uk.org.mygrid.logbook.util.ProcessRunBean;
import uk.org.mygrid.logbook.util.Utils;
import uk.org.mygrid.logbook.util.WorkflowRunBean;
import uk.org.mygrid.provenance.util.PropertyMissingException;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import de.fuberlin.wiwiss.ng4j.NamedGraph;
import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.Quad;
import de.fuberlin.wiwiss.ng4j.db.NamedGraphSetDB;
import de.fuberlin.wiwiss.ng4j.impl.NamedGraphImpl;
import de.fuberlin.wiwiss.ng4j.triql.TriQLQuery;

/**
 * Named RDF graphs repository using Named Graphs API for Jena (<a
 * href="http://www.wiwiss.fu-berlin.de/suhl/bizer/ng4j/">NG4J</a>)
 * 
 * @author dturi
 * 
 */
public class JenaMetadataService implements MetadataService {

	public static final String METADATA_HSQL_TABLES = "/metadata/hsql/tables";

	private String connectionURL;

	private String user;

	private String password;

	private String driver;

	private static Logger logger = Logger.getLogger(JenaMetadataService.class);

	private NamedGraphSetDB graphSet;

	private String storeType;

	private Properties configuration;

	public Properties getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Properties configuration) {
		this.configuration = configuration;
	}

	public JenaMetadataService() {
		// Default constructor
	}

	/**
	 * Connects to the repository specified in <code>properties</code>.
	 * 
	 * @param configuration
	 *            {@link Properties}to connect.
	 * @throws MetadataServiceCreationException
	 */
	public JenaMetadataService(Properties configuration)
			throws MetadataServiceCreationException {
		this.configuration = configuration;
		initialise();
	}

	public void initialise() throws MetadataServiceCreationException {
		storeType = configuration.getProperty(
				ProvenanceConfigurator.KAVE_TYPE_KEY,
				ProvenanceConfigurator.DEFAULT_KAVE_TYPE);
		try {
			driver = getDriver();
			connectionURL = getConnectionURL();
			user = getUser();
			password = getPassword();
			connectAndSetGraphSet();
			createGraphsMetadataGraph();
		} catch (MetadataServiceException e) {
			logger.error(e);
			throw new MetadataServiceCreationException(e);
		}
	}

	public String getDriver() {
		if (storeType.equals(ProvenanceConfigurator.JENA))
			return ProvenanceConfigurator.HSQL_JDBC_DRIVER;
		else
			return ProvenanceConfigurator.MYSQL_JDBC_DRIVER;
	}

	public String getConnectionURL() throws MetadataServiceException {
		String connectionProperty;
		if (storeType.equals(ProvenanceConfigurator.JENA))
			connectionProperty = configuration.getProperty(
					ProvenanceConfigurator.METADATA_HSQL_CONNECTION_URL,
					"jdbc:hsqldb:file:"
							+ ProvenanceConfigurator.PROVENANCE_STORE_HOME
							+ METADATA_HSQL_TABLES);
		else
			connectionProperty = configuration
					.getProperty(ProvenanceConfigurator.METADATA_MYSQL_CONNECTION_URL);
		if (connectionProperty == null) {
			try {
				ProvenanceConfigurator
						.missingPropertyMessage(ProvenanceConfigurator.METADATA_HSQL_CONNECTION_URL);
			} catch (PropertyMissingException e) {
				throw new MetadataServiceException(e);
			}
		}
		return connectionProperty;
	}

	public String getUser() throws MetadataServiceException {
		String userPropertyKey = storeType.equals(ProvenanceConfigurator.JENA) ? ProvenanceConfigurator.METADATA_HSQL_USER
				: ProvenanceConfigurator.METADATA_MYSQL_USER;
		String userProperty = configuration.getProperty(userPropertyKey);
		if (userProperty == null) {
			try {
				ProvenanceConfigurator.missingPropertyMessage(userPropertyKey);
			} catch (PropertyMissingException e) {
				throw new MetadataServiceException(e);
			}
		}
		return userProperty;
	}

	public String getPassword() {
		String passwordPropertyKey = storeType
				.equals(ProvenanceConfigurator.JENA) ? ProvenanceConfigurator.METADATA_HSQL_PASSWORD
				: ProvenanceConfigurator.METADATA_MYSQL_PASSWORD;
		return configuration.getProperty(passwordPropertyKey);
	}

	void connectAndSetGraphSet() throws MetadataServiceCreationException {
		try {
			Connection connection = connect();
			/*
			 * TODO: create a graphset with prefix "data" and one with prefix
			 * "process".
			 */
			graphSet = new NamedGraphSetDB(connection);
		} catch (ClassNotFoundException e) {
			throw new MetadataServiceCreationException(e);
		} catch (SQLException e) {
			throw new MetadataServiceCreationException(e);
		}
	}

	public Connection connect() throws ClassNotFoundException, SQLException {
		Class.forName(driver);
		Connection connection = DriverManager.getConnection(connectionURL,
				user, password);
		return connection;
	}

	public void restart() throws MetadataServiceCreationException {
		graphSet.close();
		connectAndSetGraphSet();
	}

	public void clear() throws MetadataServiceException {
		try {
			Connection connection = connect();
			NamedGraphSetDB.delete(connection);
		} catch (ClassNotFoundException e) {
			logger.error(e);
			throw new MetadataServiceException(e);
		} catch (SQLException e) {
			logger.error(e);
			throw new MetadataServiceException(e);
		}
	}

	/**
	 * Gets the <code>graphset</code>.
	 * 
	 * @return the <code>graphset</code>.
	 */
	public synchronized NamedGraphSet getGraphSet() {
		return graphSet;
		// bakendFactory.getNamedGraphSet();
	}

	void createGraphsMetadataGraph() {
		NamedGraph graphsmetadata = getGraphsMetadataGraph();
		if (graphsmetadata == null) {
			Node graphsMetadataNode = ResourceFactory.createResource(
					MetadataService.GRAPHS_METADATA).asNode();
			getGraphSet().addQuad(
					new Quad(graphsMetadataNode, new Triple(graphsMetadataNode,
							MetadataService.CREATOR_PROPERTY.asNode(),
							getCreatorNode())));
			getGraphSet().addQuad(
					new Quad(graphsMetadataNode, new Triple(graphsMetadataNode,
							MetadataService.DATE_PROPERTY.asNode(),
							getCurrentTimeLiteral())));
		}
	}

	NamedGraph getGraphsMetadataGraph() {
		return getGraphSet().getGraph(MetadataService.GRAPHS_METADATA);
	}

	public Node getCreatorNode() {
		String creator = configuration.getProperty(
				MetadataService.GRAPH_CREATOR, MetadataService.ANONYMOUS_USER);
		return ResourceFactory.createResource(creator).asNode();
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

	public void updateInstanceData(Ontology ontology, String graphName) {
		String instanceData = ontology.getInstanceDataAsString();
		logger.debug("Updating graph " + graphName + " with " + instanceData);
		updateRDFGraph(instanceData, graphName);
	}

	public void updateRDFGraph(String instanceData, String graphName) {
		updateRDFGraph(instanceData, graphName,
				ProvenanceOntology.PROVENANCE_NS);
	}

	public synchronized void updateRDFGraph(String instanceData,
			String graphName, String namespace) {
		if (logger.isDebugEnabled()) {
			logger.debug("updateRDFGraph(String instanceData=" + instanceData
					+ ", String graphName=" + graphName + ", String namespace="
					+ namespace + ") - start");
		}
		Model model = toJenaModel(instanceData, namespace);
		Model retrievedModel = retrieveGraphModel(graphName);

		if (retrievedModel != null)
			model.add(retrievedModel);
		storeModel(model, graphName);

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
	public void storeInstanceData(Ontology ontology, String graphName) {
		JenaOntology jenaOntology = ((JenaOntology) ontology);
		Model instanceData = jenaOntology.getInstanceData();
		storeModel(instanceData, graphName);
	}

	public synchronized void addQuad(String graphName, String subject,
			String predicate, String object) {
		Model model = ModelFactory.createDefaultModel();
		Resource graphNameResource = model.createResource(graphName);
		Resource subjectResource = model.createResource(subject);
		Resource predicateResource = model.createResource(predicate);
		Resource objectResource = model.createResource(object);
		logger.debug("addQuad(" + graphName + ", " + subject + ", " + predicate
				+ ", " + object + ")");
		storeQuad(graphNameResource.asNode(), subjectResource.asNode(),
				predicateResource.asNode(), objectResource.asNode());
		model.close();
	}

	public void storeQuad(Node graphName, Node subject, Node predicate,
			Node object) {
		getGraphSet().addQuad(new Quad(graphName, subject, predicate, object));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.NamedRDFGraphsPersister#storeModel(com.hp.hpl.jena.rdf.model.Model,
	 *      java.lang.String)
	 */
	public synchronized void storeModel(Model model, String graphName) {
		if (logger.isDebugEnabled()) {
			logger.debug("storeModel(Model model = " + model
					+ ", String graphName = " + graphName + ") - start");
		}

		Graph graph = model.getGraph();
		NamedGraph namedGraph = new NamedGraphImpl(graphName, graph);
		getGraphSet().addGraph(namedGraph);
		model.close();
		storeGraphMetadata(graphName);

		if (logger.isDebugEnabled()) {
			logger.debug("storeModel(Model, String) - end");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.NamedRDFGraphsPersister#storeRDFGraph(java.lang.String,
	 *      java.lang.String)
	 */
	public void storeRDFGraph(String rdfGraph, String graphName) {
		storeRDFGraph(rdfGraph, graphName, ProvenanceOntology.PROVENANCE_NS);
	}

	public void storeRDFGraph(String rdfGraph, String graphName,
			String namespace) {
		Model model = ModelFactory.createDefaultModel();
		StringReader reader = new StringReader(rdfGraph);
		model.read(reader, namespace);
		storeModel(model, graphName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.NamedRDFGraphsPersister#storeModel(java.net.URL,
	 *      java.lang.String)
	 */
	public void storeModel(URL modelURL, String graphName) throws IOException {
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
			throws IOException {
		Model tempModel = ModelFactory.createDefaultModel();
		InputStream in = modelURL.openStream();
		tempModel.read(in, namespace);
		in.close();
		storeModel(tempModel, graphName);
	}

	/**
	 * Adds creator and creation date metadata to <code>graphName</code>.
	 * 
	 * @param graphName
	 *            the name of the stored {@link com.hp.hpl.jena.graph.Graph}.
	 */
	synchronized void storeGraphMetadata(String graphName) {
		if (logger.isDebugEnabled()) {
			logger.debug("storeGraphMetadata(String graphName = " + graphName
					+ ") - start");
		}

		NamedGraph graphsmetadata = getGraphsMetadataGraph();
		Node graphNameNode = Node.create(graphName);
		Node creator = getCreatorNode();
		graphsmetadata.add(new Triple(graphNameNode,
				MetadataService.CREATOR_PROPERTY.asNode(), creator));
		Node time = getCurrentTimeLiteral();
		graphsmetadata
				.add(new Triple(graphNameNode, MetadataService.DATE_PROPERTY
						.asNode(), getCurrentTimeLiteral()));
		logger.debug("Graph: " + graphName);
		logger.debug("Creator: " + creator.getURI());
		logger.debug("Time: " + time);
	}

	static Node getCurrentTimeLiteral() {
		return toTimeLiteral(Calendar.getInstance());
	}

	static Node toTimeLiteral(Calendar date) {
		XSDDateTime dateTime = new XSDDateTime(date);
		Node timeLiteral = Node.createLiteral(dateTime.toString(), null,
				XSDDatatype.XSDdateTime);
		return timeLiteral;
	}

	/**
	 * Retrieves the graph corresponding to <code>graphName</code> and returns
	 * it as a String containing its RDF/XML representation.
	 * 
	 * @param graphName
	 *            a String.
	 * @return a String containing an RDF/XML representation of the graph.
	 */
	public String retrieveGraph(String graphName) {
		Model model = retrieveGraphModel(graphName);
		return toRDFString(model);
	}

	public static String toRDFString(Model model) {
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
	 */
	public Model retrieveGraphModel(String graphName) {
		if (logger.isDebugEnabled()) {
			logger.debug("retrieveGraphModel(String graphName = " + graphName
					+ ") - start");
		}

		NamedGraph graph = getGraphSet().getGraph(graphName);
		Model model = null;
		if (graph != null)
			model = ModelFactory.createModelForGraph(graph);

		if (logger.isDebugEnabled()) {
			logger.debug("retrieveGraphModel(String) - Model model=" + model);
			logger.debug("retrieveGraphModel(String) - end");
		}
		return model;
	}

	public Model retrieveGraphModel(String graphName, String namespace) {
		NamedGraph graph = getGraphSet().getGraph(graphName);
		Model model = null;
		if (graph != null)
			model = ModelFactory.createModelForGraph(graph);
		return model;
	}

	/**
	 * Returns an array of all names of graphs created by <code>creator</code>;
	 * 
	 * @param creator
	 *            a String identifying the creator.
	 * @return an array of Strings
	 */
	public String[] retrieveGraphsCreatedBy(String creator) {
		List<String> graphNames = new ArrayList<String>();
		String triql = "SELECT ?graph " + "WHERE "
				+ bracketify(MetadataService.GRAPHS_METADATA) + " ( ?graph "
				+ bracketify(MetadataService.CREATOR_PROPERTY.getURI()) + " "
				+ bracketify(creator) + ")";
		logger.debug(triql);
		Iterator iterator = TriQLQuery.exec(getGraphSet(), triql);
		while (iterator.hasNext()) {
			Map oneResult = (Map) iterator.next();
			Node graph = (Node) oneResult.get("graph");
			graphNames.add(graph.getURI().toString());
		}
		String[] graphArray = toStringArray(graphNames);
		return graphArray;
	}

	private String[] toStringArray(List graphNames) {
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
	 */
	public void removeGraph(String graphName) {
		if (logger.isDebugEnabled()) {
			logger.debug("removeGraph(String graphName = " + graphName
					+ ") - start");
		}

		getGraphSet().removeGraph(graphName);
		NamedGraph graphsMetadataGraph = getGraphsMetadataGraph();
		Node graphsMetadataNode = graphsMetadataGraph.getGraphName();
		Node graphNode = ResourceFactory.createResource(graphName).asNode();
		String triql = "SELECT ?p, ?o WHERE ( " + bracketify(graphName)
				+ " ?p ?o)";
		logger.debug(triql);
		Iterator iterator = TriQLQuery.exec(getGraphSet(), triql);
		while (iterator.hasNext()) {
			Map oneResult = (Map) iterator.next();
			Node p = (Node) oneResult.get("p");
			Node o = (Node) oneResult.get("o");
			Quad quad = new Quad(graphsMetadataNode, (new Triple(graphNode, p,
					o)));
			logger.debug("Removing graph metadata: " + quad.toString());
			getGraphSet().removeQuad(quad);
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

	public String getWorkflowRun(String workflowRunId)
			throws MetadataServiceException {
		Model model = getWorkflowRunModel(workflowRunId);
		String graph = toRDFString(model);
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

	public List<String> getAllWorkflowRuns() throws MetadataServiceException {
		return getIndividualsOfType(ProvenanceOntologyConstants.Classes.WORKFLOWRUN);
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

	public List<String> getAllNestedRuns() throws MetadataServiceException {
		return getIndividualsOfType(ProvenanceOntologyConstants.Classes.NESTEDWORKFLOWRUN);
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

		Node graphNode = Node.ANY;
		Node subjectNode = Node.ANY;
		Node propertyNode = Node.ANY;
		Node objectNode = Node.ANY;

		if (graph != null)
			graphNode = ResourceFactory.createResource(graph).asNode();
		if (subject != null)
			subjectNode = ResourceFactory.createResource(subject).asNode();
		if (predicate != null)
			propertyNode = ResourceFactory.createProperty(predicate).asNode();
		if (object != null)
			objectNode = ResourceFactory.createResource(object).asNode();

		Iterator iterator = getGraphSet().findQuads(graphNode, subjectNode,
				propertyNode, objectNode);
		List<String> quads = new ArrayList<String>();
		while (iterator.hasNext()) {
			Quad quad = (Quad) iterator.next();
			quads.add(quad.toString());
		}
		String[] returnStringArray = toStringArray(quads);
		if (logger.isDebugEnabled()) {
			logger
					.debug("retrieveStatementsMatchingPattern(String, String, String, String) - end");
		}
		return returnStringArray;
	}

	/**
	 * Executes <code>query</code>.
	 * 
	 * @param triqlQuery
	 *            a TriQL query String.
	 * @return the Iterator resulting from executing <code>query</code>.
	 */
	public Iterator query(String triqlQuery) {
		Iterator iterator = TriQLQuery.exec(graphSet, triqlQuery);
		return iterator;
	}

	/**
	 * Gets all the (ids of ) process runs executed by
	 * <code>workflowRunLSID</code>
	 * 
	 * @param workflowRunLSID
	 * @return List of Strings
	 */
	public List<String> getProcessesRuns(String workflowRunLSID) {
		return getFromRun(
				workflowRunLSID,
				"processRun",
				JenaProvenanceOntology
						.bracketify(ProvenanceOntologyConstants.ObjectProperties.EXECUTEDPROCESSRUN));
	}

	/*
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService#getAllProcessesRuns()
	 */
	public Map<String, Set<String>> getAllProcessesRuns()
			throws MetadataServiceException {
		Map<String, Set<String>> processRuns = new HashMap<String, Set<String>>();
		String query = "SELECT * WHERE ( ?workflowRunId <"
				+ ProvenanceVocab.EXECUTED_PROCESS_RUN.getURI()
				+ "> ?processRunId ) ";
		Iterator iterator = query(query);
		while (iterator.hasNext()) {
			Map resultMap = (Map) iterator.next();
			String workflowRunId = ((Node) resultMap.get("workflowRunId"))
					.getURI();
			String processRunId = ((Node) resultMap.get("processRunId"))
					.getURI();
			Set<String> runs = processRuns.get(workflowRunId);
			if (runs == null) {
				runs = new HashSet<String>();
				processRuns.put(workflowRunId, runs);
			}
			runs.add(processRunId);
		}

		return processRuns;
	}

	/**
	 * Gets all the (ids of ) process runs executed by
	 * <code>workflowRunLSID</code>
	 * 
	 * @param workflowRunLSID
	 * @return List of Strings
	 * @throws MetadataServiceException
	 */
	public List<String> getNonNestedProcessRuns(String workflowRunLSID)
			throws MetadataServiceException {
		List<String> processesRuns = getProcessesRuns(workflowRunLSID);
		List<String> nestedRuns = getIndividualsOfType(ProvenanceOntologyConstants.Classes.NESTEDWORKFLOWPROCESSRUN);
		processesRuns.removeAll(nestedRuns);
		return processesRuns;
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
	public List<String> getWorkflowOutputs(String workflowRunLSID) {
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

	public boolean isProcessIteration(String processURI) {
		String query = "SELECT ?value " + "WHERE ( <" + processURI
				+ "> rdf:type <"
				+ ProvenanceOntologyConstants.Classes.PROCESSITERATION + "> ) "
				+ "USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
		Iterator iterator = query(query);
		return iterator.hasNext();
	}

	public boolean isProcessWithIterations(String processURI) {
		String type = ProvenanceOntologyConstants.Classes.PROCESSRUNWITHITERATIONS;
		return isIndividualOfType(processURI, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService#getAllProcessRunsWithIterations()
	 */
	public List<String> getAllProcessRunsWithIterations()
			throws MetadataServiceException {
		return getIndividualsOfType(ProvenanceVocab.PROCESS_RUN_WITH_ITERATIONS
				.getURI());
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
				+ "WHERE ( "
				+ "<"
				+ dataLSID
				+ "> "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.OUTPUT_DATA_HAS_NAME
								.getURI()) + "?output )";
		Iterator results = query(queryString);
		if (!results.hasNext()) {
			return similarData;
		}
		Map sol = (Map) results.next();
		String output = ((Node) sol.get("output")).toString();
		queryString = "SELECT ?data, ?workflowRun, ?date "
				+ "WHERE ( ?processRun "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.PROCESS_OUTPUT.getURI())
				+ "?data . ?data "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.OUTPUT_DATA_HAS_NAME
								.getURI())
				+ " <"
				+ output
				+ "> . ?processRun "
				+ JenaProvenanceOntology.bracketify(ProvenanceVocab.END_TIME
						.getURI())
				+ " ?date )"
				+ "( ?workflowRun "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.EXECUTED_PROCESS_RUN
								.getURI()) + " ?processRun )";
		results = query(queryString);
		while (results.hasNext()) {
			sol = (Map) results.next();
			String data = ((Node) sol.get("data")).toString();
			if (data.equals(dataLSID))
				continue;
			String workflowRun = ((Node) sol.get("workflowRun")).toString();
			String date = ((Node) sol.get("date")).getLiteralValue().toString();
			Date parseDateLiteral = null;
			try {
				parseDateLiteral = Utils.parseDateLiteral(date);
			} catch (ParseException e) {
				logger.error(e);
			}
			similarData.put(data, new DataProvenance(data, workflowRun,
					parseDateLiteral));
		}
		return similarData;

	}

	/**
	 * Query to obtain all the workflow LSIDS for the current user as stated by
	 * mygrid.usercontext.experimenter
	 * 
	 * @param experimenter
	 * @return a Vector of the workflow LSIDs as Strings
	 */
	public Vector<String> getUserWorkFlows(String experimenter) {

		Vector<String> usersWorkflows = new Vector<String>();

		final String query = "SELECT ?workflowRun "
				+ "WHERE ?workflowRun ( ?workflowRun "
				+ JenaProvenanceOntology.bracketify(ProvenanceVocab.LAUNCHED_BY
						.getURI())
				+ JenaProvenanceOntology.bracketify(experimenter)
				+ ") USING ns FOR <http://www.mygrid.org.uk/provenance#>";

		Iterator itr = TriQLQuery.exec(getGraphSet(), query);

		while (itr.hasNext()) {
			Map nextMap = (Map) itr.next();
			Node workflowRun = (Node) nextMap.get("workflowRun");
			String workflowLSID = workflowRun.toString();
			usersWorkflows.add(workflowLSID);
		}

		return usersWorkflows;

	}

	public Map<String, WorkflowRunBean> getWorkflowRunBeans()
			throws MetadataServiceException {
		Map<String, WorkflowRunBean> workflowRunBeans = new HashMap<String, WorkflowRunBean>();
		Map<String, WorkflowRunBean> workflowsIdsToBeans = new HashMap<String, WorkflowRunBean>();
		Set<String> workflowInitialIds = new HashSet<String>();
		String query = "SELECT * " + "WHERE ( " + "?workflowRunId <"
				+ ProvenanceVocab.START_TIME.getURI() + "> ?date "
				+ ". ?workflowRunId <" + ProvenanceVocab.RUNS_WORKFLOW.getURI()
				+ "> ?workflow . ?workflow <"
				+ ProvenanceVocab.WORKFLOW_INITIAL_LSID.getURI()
				+ "> ?workflowInitialId " + " )";
		Iterator results = query(query);
		while (results.hasNext()) {
			Map sol = (Map) results.next();
			WorkflowRunBean workflowRunBean = new WorkflowRunBean();
			String workflowRunId = ((Node) sol.get("workflowRunId")).toString();
			workflowRunBean.setWorkflowRunId(workflowRunId);
			String date = ((Node) sol.get("date")).getLiteralValue().toString();
			workflowRunBean.setDate(date);
			workflowRunBeans.put(workflowRunId.toString(), workflowRunBean);
			String workflowInitialId = ((Node) sol.get("workflowInitialId"))
					.getLiteralValue().toString();
			workflowRunBean.setWorkflowInitialId(workflowInitialId);
			workflowInitialIds.add(workflowInitialId);
			String workflowId = sol.get("workflow").toString();
			workflowRunBean.setWorkflowId(workflowId);
			workflowsIdsToBeans.put(workflowId, workflowRunBean);
		}

		query = "SELECT * " + "WHERE ( " + "?workflow <"
				+ ProvenanceVocab.WORKFLOW_AUTHOR.getURI() + "> ?author )";
		results = query(query);
		while (results.hasNext()) {
			Map sol = (Map) results.next();
			String workflowId = sol.get("workflow").toString();
			WorkflowRunBean workflowRunBean = workflowsIdsToBeans
					.get(workflowId);
			if (workflowRunBean != null) {
				String author = ((Node) sol.get("author")).getLiteralValue()
						.toString();
				workflowRunBean.setAuthor(author);
			}
		}

		query = "SELECT * " + "WHERE ( " + "?workflow <"
				+ ProvenanceVocab.WORKFLOW_TITLE.getURI() + "> ?title )";
		results = query(query);
		while (results.hasNext()) {
			Map sol = (Map) results.next();
			String workflowId = sol.get("workflow").toString();
			WorkflowRunBean workflowRunBean = workflowsIdsToBeans
					.get(workflowId);
			if (workflowRunBean != null) {
				String title = ((Node) sol.get("title")).getLiteralValue()
						.toString();
				workflowRunBean.setTitle(title);
			}
		}

		query = "SELECT * " + "WHERE ( " + "?workflow <"
				+ ProvenanceVocab.WORKFLOW_DESCRIPTION.getURI()
				+ "> ?description )";
		results = query(query);
		while (results.hasNext()) {
			Map sol = (Map) results.next();
			String workflowId = sol.get("workflow").toString();
			WorkflowRunBean workflowRunBean = workflowsIdsToBeans
					.get(workflowId);
			if (workflowRunBean != null) {
				String description = ((Node) sol.get("description"))
						.getLiteralValue().toString();
				workflowRunBean.setDescription(description);
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
		String query = "SELECT * WHERE ( ?processRunId rdf:type <"
				+ ProvenanceVocab.FAILED_PROCESS_RUN.getURI()
				+ "> . ?processRunId <" + ProvenanceVocab.CAUSE.getURI()
				+ "> ?cause )";
		Iterator results = query(query);
		while (results.hasNext()) {
			Map sol = (Map) results.next();
			String processRunId = sol.get("processRunId").toString();
			String cause = ((Node) sol.get("cause")).getLiteralValue()
					.toString();
			failedProcessRuns.put(processRunId, cause);
		}

		Map<String, Set<ProcessRunBean>> processRunIterationsBeans = new HashMap<String, Set<ProcessRunBean>>();
		query = "SELECT * WHERE ( ?processRunId <"
				+ ProvenanceVocab.ITERATION.getURI()
				+ "> ?iteration . ?iteration <"
				+ ProvenanceVocab.END_TIME.getURI() + "> ?date . ?iteration <"
				+ ProvenanceVocab.RUNS_PROCESS.getURI()
				+ "> ?process . ?process <"
				+ ProvenanceVocab.CLASS_NAME.getURI() + "> ?processClassName )";
		results = query(query);
		while (results.hasNext()) {
			Map sol = (Map) results.next();
			String iteration = sol.get("iteration").toString();
			String date = ((Node) sol.get("date")).getLiteralValue().toString();
			String processRunId = sol.get("processRunId").toString();
			String process = ((Node) sol.get("process")).getLocalName();
			String processClassName = ((Node) sol.get("processClassName"))
					.getLiteralValue().toString();
			Set<ProcessRunBean> iterations = processRunIterationsBeans
					.get(processRunId);
			if (iterations == null) {
				iterations = new HashSet<ProcessRunBean>();
				processRunIterationsBeans.put(processRunId, iterations);
			}
			iterations.add(new ProcessRunBean(iteration, processRunId, date,
					process, processClassName));
		}
		List<String> subworkflows = getIndividualsOfType(ProvenanceVocab.NESTED_WORKFLOW_PROCESS_RUN
				.getURI());

		Map<String, Set<ProcessRunBean>> workflowRunsToProcessRuns = new HashMap<String, Set<ProcessRunBean>>();
		Map<String, ProcessRunBean> processRunBeans = new HashMap<String, ProcessRunBean>();
		query = "SELECT * WHERE ( ?workflowRunId <"
				+ ProvenanceVocab.EXECUTED_PROCESS_RUN.getURI()
				+ "> ?processRunId ) ( ?processRunId <"
				+ ProvenanceVocab.END_TIME.getURI()
				+ "> ?date . ?processRunId <"
				+ ProvenanceVocab.RUNS_PROCESS.getURI()
				+ "> ?process . ?process <"
				+ ProvenanceVocab.CLASS_NAME.getURI()
				+ "> ?processClassName ) ";

		results = query(query);
		while (results.hasNext()) {
			Map sol = (Map) results.next();
			String workflowRunId = sol.get("workflowRunId").toString();
			String processRunId = sol.get("processRunId").toString();
			String process = ((Node) sol.get("process")).getLocalName();
			String processClassName = ((Node) sol.get("processClassName"))
					.getLiteralValue().toString();
			String date = ((Node) sol.get("date")).getLiteralValue().toString();
			ProcessRunBean processRunBean = processRunBeans.get(processRunId);
			if (processRunBean == null) {
				processRunBean = new ProcessRunBean(processRunId,
						workflowRunId, date, process, processClassName);
				processRunBean.setWorkflowRunId(workflowRunId);
				processRunBeans.put(processRunId, processRunBean);
			}

			if (processRunIterationsBeans.containsKey(processRunId)) {
				processRunBean.setProcessIterations(processRunIterationsBeans
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
		return workflowRunsToProcessRuns;
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
		String query = "SELECT ?value " + "WHERE ( <" + sourceIndividual
				+ "> <" + objectProperty + "> ?value ) ";
		Iterator iterator = query(query);
		while (iterator.hasNext()) {
			Map resultMap = (Map) iterator.next();
			Node value = (Node) resultMap.get("value");
			if (!value.isBlank())
				values.add(value.getURI());
		}
		return values;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService#getPropertyValue(java.lang.String,
	 *      java.lang.String)
	 */
	public String getFirstObjectPropertyValue(String sourceIndividual,
			String objectProperty) throws MetadataServiceException {
		String query = "SELECT ?value " + "WHERE ( <" + sourceIndividual
				+ "> <" + objectProperty + "> ?value ) ";
		Iterator iterator = query(query);
		if (!iterator.hasNext())
			return null;
		Map resultMap = (Map) iterator.next();
		Node value = (Node) resultMap.get("value");
		return value.getURI();
	}

	public List<String> getDatatypePropertyValues(String sourceIndividual,
			String datatypeProperty) throws MetadataServiceException {
		List<String> values = new ArrayList<String>();
		String query = "SELECT ?value " + "WHERE ( <" + sourceIndividual
				+ "> <" + datatypeProperty + "> ?value ) ";
		Iterator iterator = query(query);
		while (iterator.hasNext()) {
			Map resultMap = (Map) iterator.next();
			Node value = (Node) resultMap.get("value");
			values.add(value.getLiteral().getValue().toString());
		}
		return values;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService#getFirstDatatypePropertyValue(java.lang.String,
	 *      java.lang.String)
	 */
	public String getFirstDatatypePropertyValue(String sourceIndividual,
			String datatypeProperty) throws MetadataServiceException {
		String query = "SELECT ?value " + "WHERE ( <" + sourceIndividual
				+ "> <" + datatypeProperty + "> ?value ) ";
		Iterator iterator = query(query);
		if (!iterator.hasNext())
			return null;
		Map resultMap = (Map) iterator.next();
		Node value = (Node) resultMap.get("value");
		return value.getLiteral().getValue().toString();
	}

	public List<String> getIndividualsOfType(String type)
			throws MetadataServiceException {
		List<String> values = new ArrayList<String>();
		String query = "SELECT ?value "
				+ "WHERE ( ?value <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"
				+ type + "> ) ";
		Iterator iterator = query(query);
		while (iterator.hasNext()) {
			Map resultMap = (Map) iterator.next();
			Node value = (Node) resultMap.get("value");
			if (!value.isBlank())
				values.add(value.getURI());
		}
		return values;
	}

	public boolean isIndividualOfType(String individual, String type) {
		String query = "SELECT ?value " + "WHERE ( <" + individual
				+ "> rdf:type <" + type + "> ) "
				+ "USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
		Iterator iterator = query(query);
		return iterator.hasNext();
	}

	private List<String> getFromRun(String workflowRunLSID, String variable,
			String property) {
		List<String> results = new ArrayList<String>();
		String query = "SELECT ?" + variable + " WHERE <" + workflowRunLSID
				+ "> ( <" + workflowRunLSID + "> " + property + "  ?"
				+ variable + " )";
		Iterator iter = query(query);
		while (iter.hasNext()) {
			Map row = (Map) iter.next();
			Node node = (Node) row.get(variable);
			String value = (String) node.getURI();
			results.add(value);
		}
		return results;
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

	static Model toJenaModel(String rdf) {
		Model tempModel = toJenaModel(rdf, ProvenanceOntology.PROVENANCE_NS);
		return tempModel;
	}

	static Model toJenaModel(String rdf, String namespace) {
		if (logger.isDebugEnabled()) {
			logger.debug("toJenaModel(String rdf=" + rdf
					+ ", String namespace=" + namespace + ") - start");
		}

		Model tempModel = ModelFactory.createDefaultModel();
		logger.debug("toJenaModel(String, String) - Model tempModel="
				+ tempModel);

		StringReader tempStringReader = new StringReader(rdf);
		logger
				.debug("toJenaModel(String, String) - StringReader tempStringReader="
						+ tempStringReader);

		tempModel.read(tempStringReader, namespace);

		if (logger.isDebugEnabled()) {
			logger.debug("toJenaModel(String, String) - end");
		}
		return tempModel;
	}
}