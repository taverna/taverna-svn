package uk.org.mygrid.sogsa.sbs;

import info.aduna.collections.iterators.CloseableIterator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.openanzo.client.DatasetService;
import org.openanzo.client.RemoteGraph;
import org.openanzo.common.exceptions.AnzoException;
import org.openanzo.model.INamedGraph;
import org.openanzo.model.impl.query.QueryResult;
import org.openanzo.services.IModelService;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.util.Template;

/**
 * Handles the addition, removal, querying and update of
 * {@link SemanticBindingInstance}s from the back end OpenAnzo database. Uses an
 * Apache Derby embedded database which writes to a file on disk, this can be
 * changed by altering the embeddedclient.properties file and having the correct
 * AnzoServer.properties file for your database of choice (see
 * http://www.openanzo.org for more details). It would also require you to
 * change the database driver in the init method
 * 
 * @author Ian Dunlop
 * 
 */
public class SemanticBindingService extends Application {

	private static DatasetService datasetService;

	public SemanticBindingService(Context parentContext) {
		super(parentContext);
		init();
	}

	/**
	 * Load the database driver using Class.forName, ask the logging to start
	 * and initialize the database
	 */
	private void init() {
		try {
			// TODO dynamically load based on anzo properties
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		} catch (InstantiationException e1) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e1.toString());
		} catch (IllegalAccessException e1) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e1.toString());
		} catch (ClassNotFoundException e1) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e1.toString());
		}

		initializeRestletLogging();
		initializeDatabase();

	}

	/**
	 * Load the open anzo database properties for the embedded client
	 */
	private void initializeDatabase() {
		Properties embeddedClientProperties = new Properties();
		try {
			embeddedClientProperties.load(SemanticBindingService.class
					.getClassLoader().getResourceAsStream(
							"embeddedclient.properties"));
		} catch (FileNotFoundException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		} catch (IOException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		}
		try {
			datasetService = new DatasetService(embeddedClientProperties);
		} catch (Exception e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		}
	}

	/**
	 * Creates all the Routes for the Restlet server. Associates urls with the
	 * classes which handle them
	 */
	@Override
	public Restlet createRoot() {
		Router router = new Router(getContext());
		
		// query all bindings via http post
		Route attach = router.attach("/sbs/query", QueryAllBindings.class);

		// get a binding via http get
		router.attach("/sbs/{binding}", SemanticBinding.class);

		// query a specific binding via http post
		router.attach("/sbs/{binding}/query", QueryBinding.class);

		// create a binding via http put or delete one with http delete
		Route sbsRoute = router.attach("/sbs", SemanticBindings.class);

//		attach.getTemplate().setMatchingMode(Template.MODE_EQUALS);
		// sbsRoute.getTemplate().setMatchingMode(Template.MODE_EQUALS);

		return router;
	}

	/**
	 * Start the restlet server logging
	 */
	private void initializeRestletLogging() {

		Handler[] handlers = java.util.logging.Logger.getLogger("")
				.getHandlers();
		for (Handler handler : handlers) {
			handler.setFormatter(new ReallySimpleFormatter());
		}
		java.util.logging.Logger.getLogger("org.mortbay.log").setLevel(
				Level.WARNING);

	}

	/**
	 * The {@link DatasetService} is the way into an OpenAnzo database. It
	 * contains all of the URIs which identify groups of triples
	 * 
	 * @return
	 */
	public static synchronized DatasetService getDatasetService() {
		return datasetService;
	}

	/**
	 * Parse the RDF using {@link Rio} and add to the open anzo database with
	 * the binding key as the context
	 * 
	 * @param rdfKey
	 * @param rdf
	 * @throws SQLException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 * @throws IOException
	 * @throws SemanticBindingException
	 * @throws AnzoException
	 * @throws Exception
	 * @throws Exception
	 */
	public void addBinding(String entityKey, String rdf) throws SQLException,
			RDFHandlerException, RDFParseException, IOException,
			SemanticBindingException, AnzoException {
		if (datasetService == null) {
			initializeDatabase();
		}
		Connection con = null;
		try {
			con = DriverManager
					.getConnection("jdbc:derby:/Users/Ian/scratch/anzoDerby");
		} catch (SQLException e1) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e1.toString());
			throw e1;
		}
		URI namedGraphURI = datasetService.getValueFactory().createURI(
				"http://" + entityKey);
		boolean createIfNecessary = true;
		INamedGraph graph = null;
		try {
			graph = datasetService.getRemoteGraph(namedGraphURI,
					createIfNecessary);

			StatementCollector sc = new StatementCollector();
			try {
				RDFParser parser = Rio.createParser(RDFFormat.RDFXML);
				parser.setRDFHandler(sc);
				parser.parse(new StringReader(rdf), "");
			} catch (UnsupportedRDFormatException mse) {
				throw mse;
			} catch (RDFHandlerException mse) {
				throw mse;
			} catch (RDFParseException mse) {
				throw mse;
			} catch (IOException mse) {
				throw mse;
			}
			try {
				datasetService.begin();
			} catch (AnzoException e) {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, e.toString());
				throw e;
			}
			try {
				for (Statement statement : sc.getStatements()) {
					graph.add(statement);
				}
			} catch (Exception e) {
				datasetService.abort();
				throw new SemanticBindingException(e);
			}
			try {
				datasetService.commit();
			} catch (AnzoException e) {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, e.toString());
				throw e;
			}
			try {
				datasetService.getDatasetReplicator().replicate(true);
			} catch (AnzoException e) {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, e.toString());
				throw e;
			}

		} catch (AnzoException e1) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e1.toString());
			throw e1;
		} finally {
			if (graph != null) {
				graph.close();
			}
		}

	}

	/**
	 * Retrieve all the RDF for a particular binding and return a
	 * {@link Binding} object which represents it. Get the named graph
	 * identifier from the database by querying for RDFIDs which match the ID
	 * represented by the passed in value key. Use this to get all of the rdf
	 * 
	 * @throws SemanticBindingException
	 * @throws AnzoException
	 * @throws NoRDFFoundException
	 * @throws SemanticBindingNotFoundException
	 */
	public SemanticBindingInstance getBinding(String key) throws AnzoException,
			NoRDFFoundException, SemanticBindingNotFoundException {
		if (datasetService == null) {
			initializeDatabase();
		}
		String rdf = new String();
		boolean hasRDF = false;

		URI namedGraphURI = datasetService.getValueFactory().createURI(
				"http://" + key);
		CloseableIterator<Statement> statements = null;
		RemoteGraph remoteGraph = null;
		try {
			remoteGraph = datasetService.getRemoteGraph(namedGraphURI, false);

			if (remoteGraph != null) {
				statements = remoteGraph.getStatements();

				if (statements != null) {
					hasRDF = true;
					while (statements.hasNext()) {
						String rdfString = statements.next().toString();
						rdf = rdf + rdfString;
					}
				}
				remoteGraph.close();
			} else {
				throw new SemanticBindingNotFoundException(
						"There was no binding " + key);
			}

			if (hasRDF) {
				return new SemanticBindingInstance(key, rdf);
			} else {
				// found a binding but no rdf, probably an anzo problem (?)
				throw new NoRDFFoundException("Binding found but no RDF");
			}
		} catch (AnzoException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
			throw e;
		} finally {
			if (remoteGraph != null) {
				remoteGraph.close();
			}
		}
	}

	public void removeBinding(String key) {
		// FIXME what should it do? Can you remove a graph or do you just delete
		// all its statements
		if (datasetService == null) {
			initializeDatabase();
		}
		URI namedGraphURI = datasetService.getValueFactory().createURI(key);
		RemoteGraph remoteGraph = null;
		try {
			remoteGraph = datasetService.getRemoteGraph(namedGraphURI, false);
		} catch (AnzoException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		}
		try {
			remoteGraph.delete(datasetService.getRemoteGraph(namedGraphURI,
					false).getStatements());
		} catch (AnzoException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		}
	}

	/**
	 * Add new RDF statements to the {@link RemoteGraph}
	 * 
	 * @param key
	 *            the unique identifier for the {@link RemoteGraph}
	 * @param rdf
	 *            the RDF to be added
	 * @throws Exception
	 */
	public void updateRDF(String key, String rdf) throws Exception {
		if (datasetService == null) {
			initializeDatabase();
		}
		URI namedGraphURI = datasetService.getValueFactory().createURI(
				"http://" + key);
		RemoteGraph remoteGraph = null;
		try {
			remoteGraph = datasetService.getRemoteGraph(namedGraphURI, false);

			// no need to delete the old stuff?
			// try {
			// remoteGraph.delete(datasetService.getRemoteGraph(namedGraphURI,
			// false).getStatements());
			// } catch (AnzoException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			StatementCollector sc = new StatementCollector();
			try {
				RDFParser parser = Rio.createParser(RDFFormat.RDFXML);
				parser.setRDFHandler(sc);
				parser.parse(new StringReader(rdf), "");
			} catch (UnsupportedRDFormatException mse) {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, mse.toString());
				throw new Exception(mse);
			} catch (RDFHandlerException mse) {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, mse.toString());
				throw new Exception(mse);
			} catch (RDFParseException mse) {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, mse.toString());
				throw new Exception(mse);
			} catch (IOException mse) {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, mse.toString());
				throw new Exception(mse);
			}

			try {
				datasetService.begin();
			} catch (AnzoException e1) {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, e1.toString());
				throw new Exception(e1);
			}
			for (Statement statement : sc.getStatements()) {
				try {
					remoteGraph.add(statement);
				} catch (Exception e) {
					java.util.logging.Logger.getLogger("org.mortbay.log").log(
							Level.WARNING,
							e.toString() + " " + statement.toString());
					throw new Exception(e);
				}
			}
			try {
				datasetService.commit();
			} catch (AnzoException e) {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, e.toString());
				throw new Exception(e);
			}
			try {
				datasetService.getDatasetReplicator().replicate(true);
			} catch (AnzoException e) {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, e.toString());
				throw new Exception(e);
			}
			try {
				remoteGraph.close();
			} catch (Exception e) {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, e.toString());
				throw new Exception(e);
			}
		} catch (AnzoException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
			throw new SemanticBindingException(e);
		} finally {
			if (remoteGraph != null) {
				remoteGraph.close();
			}
		}

	}

	/**
	 * Execute a SPARQL query on the binding specified by the key
	 * 
	 * @param key
	 *            - {@link SemanticBindingInstance} to be queried
	 * @param query
	 *            - SPARQL query to be executed
	 * @return - a string representing the query result
	 * @throws AnzoException
	 * @throws QueryEvaluationException
	 */
	public String queryBinding(String key, String query) throws AnzoException,
			QueryEvaluationException {
		if (datasetService == null) {
			initializeDatabase();
		}
		String queryResult = new String();

		URI namedGraphURI = datasetService.getValueFactory().createURI(
				"http://" + key);
		RemoteGraph remoteGraph = null;
		QueryResult result = null;
		try {
			remoteGraph = datasetService.getRemoteGraph(namedGraphURI, false);

			result = datasetService.execQuery(Collections
					.singleton(namedGraphURI), Collections.<URI> emptySet(),
					query);
		} catch (AnzoException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
			throw e;
		} finally {
			remoteGraph.close();
		}
		if (result.isAskResult()) {
			return result.getAskResult().toString();
		} else if (result.isConstructResult()) {
			Collection<Statement> constructResult = result.getConstructResult();
			if (!constructResult.isEmpty()) {
				for (Statement statement : constructResult) {
					queryResult = queryResult + statement.getSubject() + " "
							+ statement.getPredicate() + " "
							+ statement.getObject() + "\n";
				}
				return queryResult;
			}
		} else if (result.isDescribeResult()) {
			Collection<Statement> describeResult = result.getDescribeResult();
			if (!describeResult.isEmpty()) {
				for (Statement statement : describeResult) {
					queryResult = queryResult + statement.getSubject() + " "
							+ statement.getPredicate() + " "
							+ statement.getObject() + "\n";
				}
				return queryResult;
			}
		} else if (result.isSelectResult()) {
			TupleQueryResult selectResult = result.getSelectResult();
			try {
				if (selectResult.hasNext()) {
					try {
						while (selectResult.hasNext()) {
							BindingSet next = selectResult.next();
							Iterator<Binding> iterator = next.iterator();
							while (iterator.hasNext()) {
								Binding next2 = iterator.next();
								queryResult = queryResult + "Name: "
										+ next2.getName() + " Value: "
										+ next2.getValue() + "\n";
							}

						}
					} catch (QueryEvaluationException e1) {
						java.util.logging.Logger.getLogger("org.mortbay.log")
								.log(Level.WARNING, e1.toString());
					}
					return queryResult;
				}
			} catch (QueryEvaluationException e) {
				throw e;
			}
		}
		return null;
	}

	/**
	 * Look at the {@link DatasetService}, get a set of the named graphs (ie all
	 * the bindings) from the {@link IModelService} inside it and return this
	 * {@link Set}
	 * 
	 * @return a {@link Set} containing {@link URI}s representing all the
	 *         bindings
	 * @throws SemanticBindingException
	 * @throws SemanticBindingNotFoundException
	 */
	public Set<URI> getAllBindings() throws SemanticBindingException,
			SemanticBindingNotFoundException {
		if (datasetService == null) {
			initializeDatabase();
		}
		Set<URI> storedNamedGraphs = null;
		try {
			storedNamedGraphs = datasetService.getModelService()
					.getStoredNamedGraphs();

		} catch (Exception e) {
			throw new SemanticBindingException(e);
		}

		if (!storedNamedGraphs.isEmpty()) {
			return storedNamedGraphs;
		} else {
			throw new SemanticBindingNotFoundException();
		}
	}

	/**
	 * Execute a SPARQL query over all the bindings in the database. Gets the
	 * {@link Set} of named graph {@link URI}s representing the bindings in the
	 * database and runs the query over them all
	 * 
	 * @param query
	 *            SPARQL query
	 * @return a string representing the query result
	 * @throws Exception 
	 */
	public String queryAllBindings(String query) throws Exception {
		if (datasetService == null) {
			initializeDatabase();
		}
		java.util.logging.Logger.getLogger("org.mortbay.log").log(
				Level.WARNING, "querying all of the bindings");
		Set<URI> storedNamedGraphs = null;
		String queryResult = new String();
		try {
			storedNamedGraphs = datasetService.getModelService()
					.getStoredNamedGraphs();
		} catch (AnzoException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
			throw e;
		}
		QueryResult result = null;
		try {
			result = datasetService.execQuery(storedNamedGraphs, Collections
					.<URI> emptySet(), query);
		} catch (Exception e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
			throw e;
		}
		if (result.isAskResult()) {
			return result.getAskResult().toString();
		} else if (result.isConstructResult()) {
			Collection<Statement> constructResult = result.getConstructResult();
			if (!constructResult.isEmpty()) {
				for (Statement statement : constructResult) {
					queryResult = queryResult + statement.getSubject() + " "
							+ statement.getPredicate() + " "
							+ statement.getObject() + "\n";
				}
				return queryResult;
			}
		} else if (result.isDescribeResult()) {
			Collection<Statement> describeResult = result.getDescribeResult();
			if (!describeResult.isEmpty()) {
				for (Statement statement : describeResult) {
					queryResult = queryResult + statement.getSubject() + " "
							+ statement.getPredicate() + " "
							+ statement.getObject() + "\n";
				}
				return queryResult;
			}
		} else if (result.isSelectResult()) {
			TupleQueryResult selectResult = result.getSelectResult();

			try {
				if (selectResult.hasNext()) {
					while (selectResult.hasNext()) {
						BindingSet next = selectResult.next();
						Iterator<Binding> iterator = next.iterator();
						while (iterator.hasNext()) {
							Binding next2 = iterator.next();
							queryResult = queryResult + "Name: "
									+ next2.getName() + " Value: "
									+ next2.getValue() + "\n";
						}

					}
					return queryResult;
				}
			} catch (QueryEvaluationException e1) {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, e1.toString());
				throw e1;
			}
		}
		return null;
	}

	/**
	 * Remove all the bindings from the database using the OpenAnzo supplied RDF
	 * script intitializeNew.nt
	 * 
	 * @throws Exception
	 */
	public void deleteAll() throws Exception {
		if (datasetService == null) {
			initializeDatabase();
		}
		StatementCollector sc = new StatementCollector();
		try {
			RDFParser parser = Rio.createParser(RDFFormat
					.forFileName("initializeNew.nt"));
			InputStream initializationStream = SemanticBindingService.class
					.getClassLoader().getResourceAsStream("initializeNew.nt");
			parser.setRDFHandler(sc);
			parser.parse(initializationStream, "");
			datasetService.getModelService().reset(sc.getStatements());
		} catch (UnsupportedRDFormatException mse) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, mse.toString());
			throw mse;
		} catch (RDFHandlerException mse) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, mse.toString());
			throw mse;
		} catch (RDFParseException mse) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, mse.toString());
			throw mse;
		} catch (IOException mse) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, mse.toString());
			throw mse;
		} catch (AnzoException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
			throw e;
		}
		// don't think we need to do this
		// finally {
		// if (datasetService != null)
		// datasetService.close();
		//
		// }
	}

}
