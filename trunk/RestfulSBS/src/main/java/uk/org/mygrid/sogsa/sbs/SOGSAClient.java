package uk.org.mygrid.sogsa.sbs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.restlet.Client;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

/**
 * Contains methods to add bindings, update their RDF, SPARQL query individual
 * bindings, SPARQL query over all bindings, get individual bindings with their
 * RDF, get a list of all bindings and delete all bindings from the database
 * Executes all the commands using HTTP GET, PUT, POST & DELETE using the
 * {@link Restlet} framework on the {@link SemanticBindingService}, a
 * {@link Restlet} based web server, running on Jetty with an OpenAnzo RDF store
 * backend using Apache Derby. You can do all this stuff yourself using
 * something like cURL as long as you get the URL for the operations correct
 * 
 * @author Ian Dunlop
 * 
 */
public class SOGSAClient {

	public static void main(String[] args) {

		Client client = new Client(Protocol.HTTP);

		// The URI of the server
		Reference itemsUri = new Reference("http://localhost:25000/sbs");
		Reference deleteUri = new Reference("http://localhost:25000/sbs");
		Reference queryAllUri = new Reference("http://localhost:25000/sbs/query");

		// create from a http URI
		String bindingKey = null;
		URI uri = null;
		try {
			uri = new URI("http://localhost/~Ian/samplerdf2.rdf");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Response createItem2 = createItem(uri, UUID.randomUUID().toString(),
				client, itemsUri);
		if (createItem2 != null) {
			bindingKey = createItem2.getEntity().getIdentifier().toString();

			// Prints the representation of the newly created resource.
			try {
				Response response = get(client, createItem2.getEntity()
						.getIdentifier());
				if (response.getStatus().isSuccess()) {
					if (response.isEntityAvailable()) {
						response.getEntity().write(System.out);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// add some rdf
		// URI uri2 = null;
		// try {
		// uri2 = new URI("http://localhost/~Ian/samplerdf.rdf");
		// } catch (URISyntaxException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// try {
		// Response addRDF = addRDF(client, createItem2.getEntity()
		// .getIdentifier(), uri2);
		//
		// if (addRDF.getStatus().isSuccess()) {
		// Response response = get(client, createItem2.getEntity()
		// .getIdentifier());
		// if (response.getStatus().isSuccess()) {
		// if (response.isEntityAvailable()) {
		// response.getEntity().write(System.out);
		// }
		// }
		// }
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		Reference queryReference = new Reference(bindingKey + "/query");
		String query = "SELECT ?x WHERE { ?x  <http://www.recshop.fake/cd#artist>  \"Bob_Dylan\" }";
		Response queryRDF = queryRDF(client, queryReference, query);
		
		if (queryRDF.getStatus().isSuccess()) {
			System.out.println("successful query");
			if (queryRDF.isEntityAvailable()) {
				System.out.println("Entity available");
				try {
					queryRDF.getEntity().write(System.out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		Reference allBindingsRef = new Reference("http://localhost:25000/sbs");
		Response allBindings = getAllBindings(client, allBindingsRef);

		if (allBindings.getStatus().isSuccess()) {
			System.out.println("success with all bindings");
			try {
				allBindings.getEntity().write(System.out);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Response queryAllBindings = queryAllBindings(client, queryAllUri, query);
		if (queryAllBindings.getStatus().isSuccess()) {
			System.out.println("successful query");
			if (queryAllBindings.isEntityAvailable()) {
				System.out.println("Entity available");
				try {
					queryAllBindings.getEntity().write(System.out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			if (queryAllBindings.isEntityAvailable()) {
				System.out.println("Entity available");
				try {
					queryAllBindings.getEntity().write(System.out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

//		Response deleteAllBindings = deleteAllBindings(client, deleteUri);
//		if (deleteAllBindings.getStatus().isSuccess()) {
//			System.out.println("success with deleting all bindings");
//			try {
//				deleteAllBindings.getEntity().write(System.out);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		

	}

	/**
	 * Create a new binding with the rdf content in the supplied string
	 * 
	 * @param rdf
	 *            - a string containing the rdf (properly escaped etc)
	 * @param key
	 *            - user supplied unique identifier for the binding
	 * @param client
	 *            - restlet client
	 * @param itemsUri
	 *            - where the server is
	 * @return
	 */
	public static Response createItem(String rdf, String key, Client client,
			Reference itemsUri) {
		System.out.println("creating");
		// Gathering informations into a Web form.
		Form form = new Form();
		form.add("rdf", rdf);
		form.add("entityKey", key);
		// form.add("description", item.getDescription());
		Representation rep = form.getWebRepresentation();

		// Launch the request
		Response response = client.put(itemsUri, rep);

		return response;

	}

	/**
	 * Create a new binding from a file at the specified URL
	 * 
	 * @param uri
	 *            - the location of the rdf file
	 * @param key
	 *            - user supplied unique identifier for the binding
	 * @param client
	 *            - restlet client
	 * @param itemsUri
	 *            - where the server is
	 * @return
	 */
	public static Response createItem(URI uri, String key, Client client,
			Reference itemsUri) {

		Form form = new Form();
		form.add("url", uri.toString());
		form.add("entityKey", key);
		Representation rep = form.getWebRepresentation();

		Response response = client.put(itemsUri, rep);

		return response;
	}

	/**
	 * Get an individual {@link SemanticBindingInstance}
	 * 
	 * @param client
	 * @param reference
	 * @return a {@link Response} containing the RDF for the binding
	 * @throws IOException
	 */
	public static Response get(Client client, Reference reference)
			throws IOException {
		Response response = client.get(reference);
		return response;
	}

	/**
	 * Add more RDF to a binding. The RDF is contained in the string rdf (don't
	 * forget to escape it if necessary)
	 * 
	 * @param client
	 * @param rdf
	 *            - a properly escaped string with the RDF to add
	 * @param reference
	 *            - contains the URL for the operation
	 * @return
	 */
	public static Response addRDF(Client client, String rdf, Reference reference) {
		Form form = new Form();
		form.add("rdf", rdf);
		Representation rep = form.getWebRepresentation();
		Response put = client.put(reference, rep);
		return put;
	}

	/**
	 * Add more RDF to a binding. The RDF is contained at the {@link URI} called
	 * uri. The server will retreive it using http
	 * 
	 * @param client
	 * @param reference
	 *            - contains the URL for the operation
	 * @param uri
	 *            - where the RDF is to be retrieved from
	 * @return
	 */
	public static Response addRDF(Client client, Reference reference, URI uri) {
		Form form = new Form();
		form.add("url", uri.toString());
		Representation rep = form.getWebRepresentation();
		Response put = client.put(reference, rep);
		return put;
	}

	/**
	 * Execute a SPARQL query on a specific binding
	 * 
	 * @param client
	 * @param reference
	 * @param query
	 *            - a string with the SPARQL query
	 * @return
	 */
	public static Response queryRDF(Client client, Reference reference,
			String query) {
		Form form = new Form();
		form.add("query", query);
		Representation rep = form.getWebRepresentation();
		Response post = client.post(reference, rep);
		return post;
	}

	/**
	 * Gets a list of all the bindings in the database
	 * 
	 * @param client
	 * @param reference
	 * @return
	 */
	public static Response getAllBindings(Client client, Reference reference) {
		Response response = client.get(reference);
		return response;
	}

	/**
	 * Remove all the bindings from the database
	 * 
	 * @param client
	 * @param reference
	 * @return
	 */
	public static Response deleteAllBindings(Client client, Reference reference) {
		Response response = client.delete(reference);
		return response;
	}

	/**
	 * Execute a SPARQL query over all the bindings in the database
	 * 
	 * @param client
	 * @param reference
	 * @param query
	 * @return
	 */
	public static Response queryAllBindings(Client client, Reference reference,
			String query) {
		Form form = new Form();
		form.add("query", query);
		Representation rep = form.getWebRepresentation();
		Response response = client.post(reference, rep);
		return response;
	}

}
