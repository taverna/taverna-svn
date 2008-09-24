/*
 * jenaModelImpl.java
 *
 * Created on 07 March 2006, 17:46
 */

package uk.ac.man.cs.img.fetaEngine.store.impl.jena;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import uk.ac.man.cs.img.fetaEngine.command.IQueryProvider;
import uk.ac.man.cs.img.fetaEngine.command.impl.rdql.QueryProviderRDQLImpl;
import uk.ac.man.cs.img.fetaEngine.store.FetaEngineException;
import uk.ac.man.cs.img.fetaEngine.store.FetaPersistentRegistryIndex;
import uk.ac.man.cs.img.fetaEngine.webservice.CannedQueryType;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * 
 * @author Pinar
 */
public class JenaModelImpl {
	private static JenaModelImpl instance;

	private com.hp.hpl.jena.rdf.model.Model jenaModel;

	// private NamedGraphSet graphSet;
	// TO DO
	// FINISH THE IMPLEMENTATION OF THIS CLASS

	FetaPersistentRegistryIndex registryIndex;

	/** Creates a new instance of jenaModelImpl */
	public JenaModelImpl() {
		/*
		 * if (graphSet == null){
		 * 
		 * graphSet = new NamedGraphSetImpl(); }
		 */
	}

	public int publishDescription(URL operationURL) throws FetaEngineException {

		try {
			// TO DO .. write unit test for this
			String urlCont = null;
			urlCont = uk.ac.man.cs.img.fetaEngine.util.URLReader
					.getURLContentAsString(operationURL);
			if (urlCont != null) {
				return publishDescription(operationURL, urlCont);
			} else {
				return -1;
			}

		} catch (Exception exp) {
			exp.printStackTrace();

			return -1;
		}

	}

	public int publishDescription(URL operationURL, String content)
			throws FetaEngineException {

		// NamedGraph graph = graphSet.createGraph(operationURL.toString());

		// to do
		return -1;
	}

	public String freeFormQuery(String rdfQueryStatement)
			throws FetaEngineException {

		// to do
		return null;
	}

	public Set cannedQuery(CannedQueryType queryType, String paramValue)
			throws FetaEngineException {
		Set resultSet = new HashSet();

		IQueryProvider provider = new QueryProviderRDQLImpl();

		String query = provider.getQueryforCommand(queryType, paramValue);
		System.out.println(query);

		// to do
		// execute the query

		return resultSet;
	}

	public String removeDescription(URL operationURL)
			throws FetaEngineException {
		return null;

	}

	public String getStoreContent() throws FetaEngineException {

		return null;
	}

	public static JenaModelImpl getInstance() {
		System.out.println("Debug in Get Instance");
		if (instance == null) {
			instance = new JenaModelImpl();

			System.out.println("New Jena Model Impl created");
		}
		return instance;
	}

	private Model getGraphSetAsModel() {
		// return graphSet.asJenaModel("http://www.mygrid.org.uk/defaultgraph");
		return null;
	}

	private Model getInfModel() {

		return null;
	}
}
