package uk.org.mygrid.sogsa.sbs;

import info.aduna.collections.iterators.CloseableIterator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.openanzo.client.DatasetService;
import org.openanzo.common.exceptions.AnzoException;
import org.openanzo.model.INamedGraph;
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

	private Component component = null;

	private Map<String, Binding> bindingMap;

	private List<String> bindingList;

	private static DatasetService datasetService;

	public SemanticBindingService(Context parentContext) {
		super(parentContext);
		init();
		bindingMap = new HashMap<String, Binding>();
	}

	private void init() {

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
		datasetService = new DatasetService(embeddedClientProperties);
		for (URI uri : datasetService.getNamedgraphContainer().getContexts()) {
			bindingList.add(uri.getLocalName());
			// bindingMap
			// .put(uri.getLocalName(), new Binding(uri.getLocalName()));
		}

	}

	@Override
	public Restlet createRoot() {
		Router router = new Router(getContext());

		router.attach("/sbs/{binding}", SemanticBinding.class);
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
	 * @param key
	 * @param rdf
	 */
	public void addBinding(String key, String rdf) {
		bindingList.add(key);
		// bindingMap.put(key, binding);
		URI namedGraphURI = datasetService.getValueFactory().createURI(
				"http://" + key);
		boolean createIfNecessary = true;
		INamedGraph localGraph = null;
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
		for (Statement statement : sc.getStatements()) {
			graph.add(statement);
		}

	}

	public boolean hasBinding(String key) {
		return bindingList.contains(key);
		// return bindingMap.containsKey(key);
	}

	/**
	 * Retrieve all the RDF for a particular binding and return a
	 * {@link Binding} object which represents it
	 */
	public Binding getBinding(String key) {
		// do something with the datasetService to retrieve the rdf for the key
		URI namedGraphURI = datasetService.getValueFactory().createURI(
				"http://" + key);
		CloseableIterator<Statement> statements = null;
		try {
			statements = datasetService.getRemoteGraph(namedGraphURI, false)
					.getStatements();
		} catch (AnzoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Statement statement;
		String rdf = new String();
		while ((statement = statements.next()) != null) {

			rdf = rdf + statement.toString();
		}
		return new Binding(key, rdf);
	}
}
