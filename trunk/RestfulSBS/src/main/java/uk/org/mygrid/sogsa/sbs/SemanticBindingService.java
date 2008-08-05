package uk.org.mygrid.sogsa.sbs;

import info.aduna.collections.iterators.CloseableIterator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import java.util.logging.Handler;
import java.util.logging.Level;

import org.openanzo.client.DatasetService;
import org.openanzo.client.RemoteGraph;
import org.openanzo.common.exceptions.AnzoException;
import org.openanzo.model.INamedGraph;
import org.openanzo.model.impl.query.QueryResult;
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

public class SemanticBindingService extends Application {

	private static DatasetService datasetService;

	public SemanticBindingService(Context parentContext) {
		super(parentContext);
		init();
	}

	private void init() {
		try {
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

	@Override
	public Restlet createRoot() {
		Router router = new Router(getContext());

		// get a binding via http get
		router.attach("/sbs/{binding}", SemanticBinding.class);

		// query a specific binding via http put
		router.attach("/sbs/{binding}/query", QueryBinding.class);

		// create a binding via http post
		Route sbsRoute = router.attach("/sbs", SemanticBindings.class);

		// query all bindings via http put
		router.attach("/sbs/{query}", SemanticBindings.class);

		// sbsRoute.getTemplate().setMatchingMode(Template.MODE_EQUALS);

		return router;
	}

	private void initializeRestletLogging() {

		Handler[] handlers = java.util.logging.Logger.getLogger("")
				.getHandlers();
		for (Handler handler : handlers) {
			handler.setFormatter(new ReallySimpleFormatter());
		}
		java.util.logging.Logger.getLogger("org.mortbay.log").setLevel(
				Level.WARNING);

	}

	public static synchronized DatasetService getDatasetService() {
		return datasetService;
	}

	/**
	 * Parse the RDF using {@link Rio} and add to the open anzo database with
	 * the binding key as the context
	 * 
	 * @param rdfKey
	 * @param rdf
	 */
	public void addBinding(String entityKey, String rdf) {
		java.util.logging.Logger.getLogger("org.mortbay.log").log(
				Level.WARNING, "key is: " + entityKey);
		Connection con = null;
		try {
			con = DriverManager
					.getConnection("jdbc:derby:/Users/Ian/scratch/anzoDerby");
		} catch (SQLException e1) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e1.toString());
		}
		URI namedGraphURI = datasetService.getValueFactory().createURI(
				"http://" + entityKey);
		boolean createIfNecessary = true;
		INamedGraph graph = null;
		try {
			graph = datasetService.getRemoteGraph(namedGraphURI,
					createIfNecessary);

		} catch (AnzoException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		}
		StatementCollector sc = new StatementCollector();
		try {
			RDFParser parser = Rio.createParser(RDFFormat.RDFXML);
			parser.setRDFHandler(sc);
			parser.parse(new StringReader(rdf), "");
		} catch (UnsupportedRDFormatException mse) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, mse.toString());
		} catch (RDFHandlerException mse) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, mse.toString());
		} catch (RDFParseException mse) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, mse.toString());
		} catch (IOException mse) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, mse.toString());
		}
		try {
			datasetService.begin();
		} catch (AnzoException e1) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e1.toString());
		}
		for (Statement statement : sc.getStatements()) {
			graph.add(statement);
		}
		try {
			datasetService.commit();
		} catch (AnzoException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		}
		try {
			datasetService.getDatasetReplicator().replicate(true);
		} catch (AnzoException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		}
		graph.close();

	}

	/**
	 * Retrieve all the RDF for a particular binding and return a
	 * {@link Binding} object which represents it. Get the named graph
	 * identifier from the database by querying for RDFIDs which match the ID
	 * represented by the passed in value key. Use this to get all of the rdf
	 * 
	 * @throws SemanticBindingException
	 */
	public SemanticBindingInstance getBinding(String key)
			throws SemanticBindingException {

		java.util.logging.Logger.getLogger("org.mortbay.log").log(
				Level.WARNING, "got key: " + key);

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
			}
			remoteGraph.close();

			if (hasRDF) {
				return new SemanticBindingInstance(key, rdf);
			}
			// no binding so if we haven't thrown an exception for some reason
			// then......
			return null;
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

	public void removeBinding(String key) {
		// FIXME what should it do? Can you remove a graph or do you just delete
		// all its statements

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

	public String queryBinding(String key, String query) {

		String queryResult = new String();

		URI namedGraphURI = datasetService.getValueFactory().createURI(
				"http://" + key);
		RemoteGraph remoteGraph = null;
		try {
			remoteGraph = datasetService.getRemoteGraph(namedGraphURI, false);
		} catch (AnzoException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		}
		QueryResult result = datasetService.execQuery(Collections
				.singleton(namedGraphURI), Collections.<URI> emptySet(), query);
		if (result.isAskResult()) {
			return result.getAskResult().toString();
		} else if (result.isConstructResult()) {
			Collection<Statement> constructResult = result.getConstructResult();
			for (Statement statement : constructResult) {
				queryResult = queryResult + statement.getSubject() + " "
						+ statement.getPredicate() + " "
						+ statement.getObject() + "\n";
			}
			return queryResult;
		} else if (result.isDescribeResult()) {
			Collection<Statement> describeResult = result.getDescribeResult();
			for (Statement statement : describeResult) {
				queryResult = queryResult + statement.getSubject() + " "
						+ statement.getPredicate() + " "
						+ statement.getObject() + "\n";
			}
			return queryResult;
		} else if (result.isSelectResult()) {
			TupleQueryResult selectResult = result.getSelectResult();

			try {
				while (selectResult.hasNext()) {
					BindingSet next = selectResult.next();
					Iterator<Binding> iterator = next.iterator();
					while (iterator.hasNext()) {
						Binding next2 = iterator.next();
						queryResult = queryResult + "Name: " + next2.getName()
								+ " Value: " + next2.getValue() + "\n";
					}

				}
			} catch (QueryEvaluationException e1) {
				java.util.logging.Logger.getLogger("org.mortbay.log").log(
						Level.WARNING, e1.toString());
			}
			return queryResult;
		}
		return null;
	}

	private ResultSet getBindingForEntityKey(String key) {
		Connection con = null;
		try {
			con = DriverManager
					.getConnection("jdbc:derby:/Users/Ian/scratch/entity");
		} catch (SQLException e1) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e1.toString());
		}
		java.sql.Statement sqlStatement = null;
		try {
			sqlStatement = con.createStatement();
		} catch (SQLException e2) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e2.toString());
		}

		String sql = "SELECT rdfid FROM identifier WHERE id=" + key;
		ResultSet executeQuery = null;
		try {
			executeQuery = sqlStatement.executeQuery(sql);
		} catch (SQLException e2) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e2.toString());
		}
		return executeQuery;
	}

	/**
	 * Load the rdf from the supplied URL
	 * 
	 * @param url
	 * @return
	 */
	private String loadRDF(String url) {
		String rdfString = new String();
		URL rdfURL = null;
		try {
			rdfURL = new URL(url);
		} catch (MalformedURLException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		}

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(rdfURL.openStream()));
		} catch (IOException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		}

		String inputLine;

		try {
			while ((inputLine = in.readLine()) != null)
				rdfString = rdfString + inputLine;
		} catch (IOException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		}

		try {
			in.close();
		} catch (IOException e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		}
		return rdfString;
	}

}
