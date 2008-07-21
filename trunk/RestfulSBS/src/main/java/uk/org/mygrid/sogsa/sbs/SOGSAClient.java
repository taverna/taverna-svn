package uk.org.mygrid.sogsa.sbs;

import java.io.IOException;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

public class SOGSAClient {

	public static void main(String[] args) {

		Client client = new Client(Protocol.HTTP);

		// The URI of the resource "list of items".
		Reference itemsUri = new Reference(
				"http://localhost:25000/sbs");
		String rdf = "<rdf:RDF  "
				+ " xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" "
				+ " xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
				+ " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"> "
				+ "<foaf:Person rdf:nodeID=\"me\">"
				+ "<foaf:name>Person</foaf:name>"
				+ "<foaf:title>Mr</foaf:title>"
				+ "<foaf:givenname>wibl</foaf:givenname>"
				+ "<foaf:family_name>WOBL</foaf:family_name>"
				+ "<foaf:phone rdf:resource=\"tel:12345\"/>"
				+ " <foaf:homepage>        "
				+ "   <rdf:Description rdf:about=\"http://www.cs.man.ac.uk/~penpecip/\">     "
				+ "     <dc:title>Pinar ALPERs Research Homepage</dc:title>      "
				+ "     <dc:description>My HomePage</dc:description>     "
				+ "   </rdf:Description> "
				+ " </foaf:homepage>        "
				+ " <foaf:currentProject>      "
				+ "  <rdf:Description rdf:about=\"http://www.ontogrid.net/\">    "
				+ "      <dc:title>OntoGrid</dc:title>        "
				+ "          <dc:description>Working as an RA in Ontogrid project</dc:description>  "
				+ "      </rdf:Description>  "
				+ "</foaf:currentProject>   "
				+ "<foaf:pastProject>       "
				+ "  <rdf:Description rdf:about=\"http://www.mygrid.org.uk/\"> "
				+ "         <dc:title>myGrid</dc:title>        "
				+ "         <dc:description>I previously worked for the UK e-Science pilot project myGrid</dc:description>  "
				+ "      </rdf:Description>    " + "</foaf:pastProject>"
				+ "</foaf:Person>" + "</rdf:RDF>";
		Reference createItem = createItem(rdf, client, itemsUri);
		if (createItem != null) {

			// Prints the representation of the newly created resource.
			try {
				get(client, createItem);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static Reference createItem(String rdf, Client client,
			Reference itemsUri) {
		System.out.println("creating");
		// Gathering informations into a Web form.
		Form form = new Form();
		form.add("rdf", rdf);
		// form.add("description", item.getDescription());
		Representation rep = form.getWebRepresentation();

		// Launch the request
		Response response = client.post(itemsUri, rep);
		if (response.getStatus().isSuccess()) {
			if (response.isEntityAvailable()) {
				try {
					// Always consume the response's entity, if available.
					System.out.println("url for binding is: "
							+ response.getEntity().getIdentifier().toString());
					response.getEntity().write(System.out);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return response.getEntity().getIdentifier();
		}

		return null;
	}

	public static void get(Client client, Reference reference)
			throws IOException {
		System.out.println("getting");
		Response response = client.get(reference);
		if (response.getStatus().isSuccess()) {
			if (response.isEntityAvailable()) {
				response.getEntity().write(System.out);
			}
		}
	}

}
