package uk.org.mygrid.sogsa.sbs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.restlet.Client;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
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

	private static final String QUERY_URL_SUFFIX = "/query"; //$NON-NLS-1$
	private static final String HTTP_LOCALHOST_25000_SBS = ClientConfig
			.getString("SOGSAClient.1"); //$NON-NLS-1$
	private static final String SAMPLE_SPARQL_QUERY_1 = ClientConfig
			.getString("SOGSAClient.2"); //$NON-NLS-1$
	private static final String TEST_RDF_FILE_1 = ClientConfig
			.getString("SOGSAClient.3"); //$NON-NLS-1$
	private static final String TEST_RDF_FILE_2 = ClientConfig
			.getString("SOGSAClient.4"); //$NON-NLS-1$

	/**
	 * main contains a number of client usage snippets to illustrate a typical
	 * sequence: create a SB - incrementally populate SB - query SB - delete SB
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Client client = new Client(Protocol.HTTP);

		// The URI of the server
		Reference itemsUri = new Reference(HTTP_LOCALHOST_25000_SBS);
		Reference deleteUri = new Reference(HTTP_LOCALHOST_25000_SBS);
		Reference queryAllUri = new Reference(HTTP_LOCALHOST_25000_SBS
				+ QUERY_URL_SUFFIX);

		// ////////
		// 1
		// create a new SB from a http URI pointing to the RDF content.
		// (alternatively, you can supply the RDF content inline)
		// the SB consists of the pair (<binding key>, <RDF statements>) where
		// <binding key> Grid Entity key that will be provided by Karma
		// and corresponds to the "workflow user session", so that all RDF
		// fragments
		// for that session are added to this SB
		// ////////

		System.out
				.println("1: create a new SB from a http URI pointing to the RDF content.");

		String bindingKey = null;
		URI uri = null;
		try {
			uri = new URI(TEST_RDF_FILE_1);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// the construct "http:"+ UUID.randomUUID().toString()
		// stands for the actual binding key provided by Karma
		Response createItem2 = createItem(loadRDF(TEST_RDF_FILE_1), "http://" //$NON-NLS-1$
				+ UUID.randomUUID().toString(), client, itemsUri);
		if (createItem2 != null) {

			bindingKey = createItem2.getEntity().getIdentifier().toString();

			// Prints the representation of the newly created resource.
			try {
				Response response = get(client, createItem2.getEntity()
						.getIdentifier());
				try {
					if (response.getStatus().isSuccess()) {
						if (response.isEntityAvailable()) {
							// System.out.println(
							// "here is the RDF for the new SB:");
							// response.getEntity().write(System.out);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					// if you don't get the entity to write out it's stream you
					// have to close it, otherwise the connection to the server
					// never closes and you will never get to talk to it again
					response.getEntity().getStream().close();
				}

			} catch (Exception e) {

			} finally {
				try {
					createItem2.getEntity().getStream().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// ////////
		// 2
		// add more rdf statements to the SB we just created -- note we are
		// referring back to the same SB using the binding key
		// (obtained by createItem2)
		// ////////

		System.out
				.println("2 - add more rdf statements to the SB we just created");
		URI uri2 = null;
		try {
			uri2 = new URI(TEST_RDF_FILE_2);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(createItem2.getEntity().getIdentifier());
		Response addRDF = addRDF(client, createItem2.getEntity()
				.getIdentifier(), uri2);
		try {
			if (addRDF.getStatus().isSuccess()) {
				System.out.println("success");
				Response response = get(client, createItem2.getEntity()
						.getIdentifier());
				try {
					if (response.getStatus().isSuccess()) {
						if (response.isEntityAvailable()) {
							// System.out.println(response.getEntity());
							// here we have written the contents of the entities
							// stream out so it is now closed
							response.getEntity().write(System.out);
						}
					}
				} catch (Exception e) {

				} finally {
					// response.getEntity().getStream().close();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		} finally {
			try {
				addRDF.getEntity().getStream().close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// ////////
		// 3
		// query the RDF corresponding to our binding key, using SPARQL
		// ////////

		System.out
				.println("3: query the RDF corresponding to our binding key, using SPARQL");

		Reference queryReference = new Reference(bindingKey + QUERY_URL_SUFFIX);
		String queryFile = SAMPLE_SPARQL_QUERY_1;
		String query = loadRDF(queryFile);
		System.out.println("The query is: " + query);
		// InputStream resourceAsStream = SOGSAClient.class.getClassLoader()
		// .getResourceAsStream(SAMPLE_SPARQL_QUERY_1);
		// String query = new String();
		// try {
		// BufferedReader br = new BufferedReader(new InputStreamReader(
		// resourceAsStream));
		//
		// String inputLine;
		//
		// while ((inputLine = br.readLine()) != null)
		// query = query + inputLine;
		//
		// } catch (FileNotFoundException e1) {
		// // TODO Auto-generated catch block
		// System.out.println(e1);
		// e1.printStackTrace();
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// System.out.println(e);
		// e.printStackTrace();
		// }

		Response queryRDF = queryRDF(client, queryReference, query);
		try {
			if (queryRDF.getStatus().isSuccess()) {
				System.out.println("successful query"); //$NON-NLS-1$
				if (queryRDF.isEntityAvailable()) {
					System.out.println("Entity available"); //$NON-NLS-1$

					if (queryRDF.getStatus().equals(Status.SUCCESS_NO_CONTENT)) {
						System.out.println("... but empty response"); //$NON-NLS-1$
					} else {
						try {
							queryRDF.getEntity().write(System.out);
						} catch (IOException e5) {
							// TODO Auto-generated catch block
							e5.printStackTrace();
						}
					}
				}
			} else {
				System.out.println("query failed");
			}
		} catch (Exception e) {

		} finally {
			// try {
			// queryRDF.getEntity().getStream().close();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}

		// ////////
		// 4
		// retrieve all available binding keys from the service
		// ////////

		Reference allBindingsRef = new Reference(HTTP_LOCALHOST_25000_SBS);
		Response allBindings = getAllBindings(client, allBindingsRef);

		try {
			if (allBindings.getStatus().isSuccess()) {
				System.out.println("success with all bindings"); //$NON-NLS-1$

				if (allBindings.getStatus().equals(Status.SUCCESS_NO_CONTENT)) {
					System.out.println("... but empty response"); //$NON-NLS-1$
				} else {
					try {
						allBindings.getEntity().write(System.out);
					} catch (IOException e0) {
						// TODO Auto-generated catch block
						e0.printStackTrace();
					}
				}
			}
		} catch (Exception e) {

		} finally {
			// try {
			// allBindings.getEntity().getStream().close();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}

		// ////////
		// 5
		// apply a SPARQL query to all RDF graphs available form the server
		// CHECK there is an issue here that is being discussed with the Anzo
		// people -- should work in Anzo 3.0 but not in 2.5.1
		// ////////
		queryFile = SAMPLE_SPARQL_QUERY_1;
		String loadRDF = loadRDF(queryFile);

		Response queryAllBindings = queryAllBindings(client, queryAllUri,
				loadRDF);
		try {
			if (queryAllBindings.getStatus().equals(Status.SUCCESS_NO_CONTENT)) {
				System.out.println("\nThere were no query results"); //$NON-NLS-1$
			} else if (queryAllBindings.getStatus().equals(Status.SUCCESS_OK)) {
				System.out.println("\nsuccessful query"); //$NON-NLS-1$
				if (queryAllBindings.isEntityAvailable()) {
					try {
						queryAllBindings.getEntity().write(System.out);
					} catch (IOException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
				}
			} else {
				System.out.println("\nQuery failed"); //$NON-NLS-1$
			}
		} catch (Exception e) {

		} finally {
			// try {
			// queryAllBindings.getEntity().getStream().close();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}

		// ////////
		// 6
		// zap the database
		// ////////
		Response deleteAllBindings = deleteAllBindings(client, deleteUri);
		try {
			if (deleteAllBindings.getStatus().isSuccess()) {
				System.out.println("success with deleting all bindings"); //$NON-NLS-1$
				try {
					deleteAllBindings.getEntity().write(System.out);
				} catch (IOException e9) {
					// TODO Auto-generated catch block
					e9.printStackTrace();
				}
			}

		} catch (Exception e) {

		} finally {
			// try {
			// deleteAllBindings.getEntity().getStream().close();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}

	}

	// ///////////
	// end of client usage snippets
	// //////////

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
		System.out.println("creating"); //$NON-NLS-1$
		// Gathering informations into a Web form.
		Form form = new Form();
		form.add("rdf", rdf); //$NON-NLS-1$
		form.add("entityKey", key); //$NON-NLS-1$
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
		form.add("url", uri.toString()); //$NON-NLS-1$
		form.add("entityKey", key); //$NON-NLS-1$
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
		form.add("rdf", rdf); //$NON-NLS-1$
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
		System.out.println("add rdf: " + uri.toString());
		Form form = new Form();
		form.add("url", uri.toString()); //$NON-NLS-1$
		Representation rep = form.getWebRepresentation();
		Response put = null;
		try {
			System.out.println("making call");
			put = client.put(reference.toString(), rep);
			// put = client.put(reference.toString(), rep);
			System.out.println("hello " + put);
		} catch (Exception e) {
			System.out.println(e);
		}
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
		form.add("query", query); //$NON-NLS-1$
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
		form.add("query", query); //$NON-NLS-1$
		Representation rep = form.getWebRepresentation();
		Response response = client.post(reference, rep);
		return response;
	}

	private static String loadRDF(String file) {
		InputStream resourceAsStream = SOGSAClient.class.getClassLoader()
				.getResourceAsStream(file);
		String rdfString = new String();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				resourceAsStream));
		String inputLine;

		try {
			while ((inputLine = in.readLine()) != null)
				rdfString = rdfString + inputLine;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rdfString;
	}

}
