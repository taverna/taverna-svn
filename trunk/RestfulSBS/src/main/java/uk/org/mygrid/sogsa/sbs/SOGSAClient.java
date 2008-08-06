package uk.org.mygrid.sogsa.sbs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

public class SOGSAClient {

	public static void main(String[] args) {

		Client client = new Client(Protocol.HTTP);

		// The URI of the server
		Reference itemsUri = new Reference("http://localhost:25000/sbs");
		Reference deleteUri = new Reference("http://localhost:25000/sbs");

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
//		URI uri2 = null;
//		try {
//			uri2 = new URI("http://localhost/~Ian/samplerdf.rdf");
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			Response addRDF = addRDF(client, createItem2.getEntity()
//					.getIdentifier(), uri2);
//
//			if (addRDF.getStatus().isSuccess()) {
//				Response response = get(client, createItem2.getEntity()
//						.getIdentifier());
//				if (response.getStatus().isSuccess()) {
//					if (response.isEntityAvailable()) {
//						response.getEntity().write(System.out);
//					}
//				}
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
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
		
		Response deleteAllBindings = deleteAllBindings(client, deleteUri);
		if (deleteAllBindings.getStatus().isSuccess()) {
			System.out.println("success with deleting all bindings");
			try {
				deleteAllBindings.getEntity().write(System.out);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

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

	public static Response get(Client client, Reference reference)
			throws IOException {
		Response response = client.get(reference);
		return response;
	}

	public static Response addRDF(Client client, String rdf, Reference reference) {
		Form form = new Form();
		form.add("rdf", rdf);
		Representation rep = form.getWebRepresentation();
		Response put = client.put(reference, rep);
		return put;
	}

	public static Response addRDF(Client client, Reference reference, URI uri) {
		Form form = new Form();
		form.add("url", uri.toString());
		Representation rep = form.getWebRepresentation();
		Response put = client.put(reference, rep);
		return put;
	}
	
	public static Response queryRDF(Client client, Reference reference, String query) {
		Form form = new Form();
		form.add("query", query);
		Representation rep = form.getWebRepresentation();
		Response post = client.post(reference, rep);
		return post;
	}
	
	public static Response getAllBindings(Client client, Reference reference) {
		Response response = client.get(reference);
		return response;
	}
	
	public static Response deleteAllBindings(Client client, Reference reference) {
		Response response = client.delete(reference);
		return response;
	}
	

}
