package uk.org.mygrid.sogsa.sbs;

import info.aduna.collections.iterators.CloseableIterator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
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
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.resource.Resource;

public class SemanticBindingService extends Application {

	// private List<String> bindingList;

	private static DatasetService datasetService;

	public SemanticBindingService(Context parentContext) {
		super(parentContext);
		init();
		// bindingList = new ArrayList<String>();
	}

	private void init() {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// read all the binding keys from the database and intialise the list

		initializeRestletLogging();
		Properties embeddedClientProperties = new Properties();
		try {
			embeddedClientProperties.load(SemanticBindingService.class
					.getClassLoader().getResourceAsStream(
							"embeddedclient.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			datasetService = new DatasetService(embeddedClientProperties);
		} catch (Exception e) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, e.toString());
		}
		// for (URI uri : datasetService.getNamedgraphContainer().getContexts())
		// {
		// java.util.logging.Logger.getLogger("org.mortbay.log").log(
		// Level.WARNING, uri.getLocalName());
		// bindingList.add(uri.getLocalName());
		// }

	}

	@Override
	public Restlet createRoot() {
		Router router = new Router(getContext());
		// get a binding
		router.attach("/sbs/{binding}", SemanticBinding.class);

		router.attach("/sbs/{binding}/query", SemanticBinding.class);
		// create a binding
		Route sbsRoute = router.attach("/sbs", SemanticBindings.class);
		// sbsRoute.getTemplate().setMatchingMode(Template.MODE_EQUALS);

		return router;
	}

	// /**
	// * Creates the RESTFUL {@link Component} which handles all the routes,
	// * logging etc
	// *
	// * @return
	// */
	// private Component createComponent() {
	// Component component = new Component();
	// component.getServers().add(Protocol.HTTP, 25000);
	// attachRoutes(component);
	// return component;
	// }

	/**
	 * Associate URLs with the {@link Resource}s which will deal with any
	 * RESTFUL calls to them
	 * 
	 * @param component
	 */
	private void attachRoutes(Component component) {

		// /sbs create a binding using the SemanticBindings class
		component.getDefaultHost().attach("/sbs", SemanticBindings.class);
		// /sbs/UUID get a binding using the SemanticBinding class
		// class
		component.getDefaultHost().attach("/{binding}/", SemanticBinding.class);
		// /sbs/UUID/add update the RDF in a binding
		// component.getDefaultHost().attach("/sbs/{binding}/add",
		// RDFUpdate.class);

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
	public void addBinding(String entityKey, String rdfKey, String rdf) {
		Connection con = null;
		try {
			con = DriverManager
					.getConnection("jdbc:derby:/Users/Ian/scratch/entity");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		java.sql.Statement sqlStatement = null;
		try {
			sqlStatement = con.createStatement();
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		String sql = "INSERT INTO identifier(entityKey, key) VALUES";
		try {
			sqlStatement.executeUpdate(sql);
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// bindingList.add(key);
		// bindingMap.put(key, binding);
		URI namedGraphURI = datasetService.getValueFactory().createURI(
				"http://" + rdfKey);
		boolean createIfNecessary = true;
		INamedGraph graph = null;
		try {
			graph = datasetService.getRemoteGraph(namedGraphURI,
					createIfNecessary);

		} catch (AnzoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StatementCollector sc = new StatementCollector();
		try {
			RDFParser parser = Rio.createParser(RDFFormat.RDFXML);
			parser.setRDFHandler(sc);
			parser.parse(new StringReader(rdf), "");
		} catch (UnsupportedRDFormatException mse) {
			throw new RuntimeException(mse);
		} catch (RDFHandlerException mse) {
			throw new RuntimeException(mse);
		} catch (RDFParseException mse) {
			throw new RuntimeException(mse);
		} catch (IOException mse) {
			throw new RuntimeException(mse);
		}
		try {
			datasetService.begin();
		} catch (AnzoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (Statement statement : sc.getStatements()) {
			java.util.logging.Logger.getLogger("org.mortbay.log").log(
					Level.WARNING, statement.toString());
			graph.add(statement);
		}
		try {
			datasetService.commit();
		} catch (AnzoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			datasetService.getDatasetReplicator().replicate(true);
		} catch (AnzoException e) {
			e.printStackTrace();
		}
		graph.close();

	}

	// public boolean hasBinding(String key) {
	// java.util.logging.Logger.getLogger("org.mortbay.log").log(
	// Level.WARNING, "looking for a binding");
	// if (bindingList.contains(key)) {
	// return true;
	// }
	// java.util.logging.Logger.getLogger("org.mortbay.log").log(
	// Level.WARNING, "there was no binding");
	// return false;
	// // return bindingMap.containsKey(key);
	// }

	/**
	 * Retrieve all the RDF for a particular binding and return a
	 * {@link Binding} object which represents it. Get the named graph
	 * identifier from the database by querying for RDFIDs which match the ID
	 * represented by the passed in value key. Use this to get all of the rdf
	 */
	public Binding getBinding(String key) {

		ResultSet executeQuery = getBindingForEntityKey(key);

		String rdf = new String();
		boolean hasRDF = false;
		try {
			while (executeQuery.next()) {
				String uniqueKey = executeQuery.getString(0);
				URI namedGraphURI = datasetService.getValueFactory().createURI(
						"http://" + uniqueKey);
				CloseableIterator<Statement> statements = null;
				RemoteGraph remoteGraph = null;
				try {
					remoteGraph = datasetService.getRemoteGraph(namedGraphURI,
							false);

					// datasetService.getRemoteGraph(namedGraphURI,
					// false).getMetaDataGraph().
					// datasetService.getRemoteGraph(namedGraphURI, false).
				} catch (AnzoException e) {
					java.util.logging.Logger.getLogger("org.mortbay.log").log(
							Level.WARNING, e.toString());
				}
				if (remoteGraph != null) {

					if (statements != null) {
						hasRDF = true;
						while (statements.hasNext()) {
							// java.util.logging.Logger.getLogger("org.mortbay.log").log(
							// Level.WARNING, statements.next().toString());
							rdf = rdf + statements.next().toString();
						}
					}
				}
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// do something with the datasetService to retrieve the rdf for the key

		// URI namedGraphURI = datasetService.getValueFactory().createURI(
		// "http://" + key);
		// CloseableIterator<Statement> statements = null;
		// try {
		// statements = datasetService.getRemoteGraph(namedGraphURI, false)
		// .getStatements();
		// // datasetService.getRemoteGraph(namedGraphURI,
		// // false).getMetaDataGraph().
		// // datasetService.getRemoteGraph(namedGraphURI, false).
		// } catch (AnzoException e) {
		// java.util.logging.Logger.getLogger("org.mortbay.log").log(
		// Level.WARNING, e.toString());
		// }
		// String rdf = new String();
		//
		// if (statements != null) {
		//
		// while (statements.hasNext()) {
		// // java.util.logging.Logger.getLogger("org.mortbay.log").log(
		// // Level.WARNING, statements.next().toString());
		// rdf = rdf + statements.next().toString();
		// }

		// Statement statement;
		// while ((statement = statements.next()) != null) {
		//
		// rdf = rdf + statement.toString();
		// }

		if (hasRDF) {
			return new Binding(key, rdf);
		}
		return null;
	}

	public void removeBinding(String key) {
		// FIXME what should it do? Can you remove a graph or do you just delete
		// all its statements
		ResultSet executeQuery = getBindingForEntityKey(key);
		try {
			while (executeQuery.next()) {
				String uniqueKey = executeQuery.getString(0);

				URI namedGraphURI = datasetService.getValueFactory().createURI(
						"http://" + uniqueKey);
				RemoteGraph remoteGraph = null;
				try {
					remoteGraph = datasetService.getRemoteGraph(namedGraphURI,
							false);
				} catch (AnzoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					remoteGraph.delete(datasetService.getRemoteGraph(
							namedGraphURI, false).getStatements());
				} catch (AnzoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Add new RDF statements to the {@link RemoteGraph}
	 * 
	 * @param key
	 *            the unique identifier for the {@link RemoteGraph}
	 * @param rdf
	 *            the RDF to be added
	 */
	public void updateRDF(String key, String rdf) {
		URI namedGraphURI = datasetService.getValueFactory().createURI(
				"http://" + key);
		RemoteGraph remoteGraph = null;
		try {
			remoteGraph = datasetService.getRemoteGraph(namedGraphURI, false);
		} catch (AnzoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			throw new RuntimeException(mse);
		} catch (RDFHandlerException mse) {
			throw new RuntimeException(mse);
		} catch (RDFParseException mse) {
			throw new RuntimeException(mse);
		} catch (IOException mse) {
			throw new RuntimeException(mse);
		}
		try {
			datasetService.begin();
		} catch (AnzoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (Statement statement : sc.getStatements()) {
			// java.util.logging.Logger.getLogger("org.mortbay.log").log(
			// Level.WARNING, statement.toString());
			remoteGraph.add(statement);
		}
		try {
			datasetService.commit();
		} catch (AnzoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			datasetService.getDatasetReplicator().replicate(true);
		} catch (AnzoException e) {
			e.printStackTrace();
		}
		remoteGraph.close();

	}

	public String queryBinding(String key, String query) {
		ResultSet executeQuery = getBindingForEntityKey(key);
		
		try {
			while (executeQuery.next()) {
				String uniqueKey = executeQuery.getString(0);

				URI namedGraphURI = datasetService.getValueFactory().createURI(
						"http://" + uniqueKey);
				RemoteGraph remoteGraph = null;
				try {
					remoteGraph = datasetService.getRemoteGraph(namedGraphURI,
							false);
				} catch (AnzoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				QueryResult result = datasetService.execQuery(Collections
						.singleton(namedGraphURI),
						Collections.<URI> emptySet(), query);
				if (result.isAskResult()) {
					result.getAskResult();
				} else if (result.isConstructResult()) {
					result.getConstructResult();
				} else if (result.isDescribeResult()) {
					result.getDescribeResult();
				} else if (result.isSelectResult()) {
					result.getSelectResult();
				}
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// StringWriter writer = new StringWriter();
		// new SPARQLResultsXMLWriter(result);
		// return writer.toString();
		return null;
	}
	
	private ResultSet getBindingForEntityKey(String key) {
		Connection con = null;
		try {
			con = DriverManager
					.getConnection("jdbc:derby:/Users/Ian/scratch/entity");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		java.sql.Statement sqlStatement = null;
		try {
			sqlStatement = con.createStatement();
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		String sql = "SELECT rdfid FROM identifier WHERE id=" + key;
		ResultSet executeQuery = null;
		try {
			executeQuery = sqlStatement.executeQuery(sql);
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		return executeQuery;
	}

}
