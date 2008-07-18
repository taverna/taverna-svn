package uk.org.mygrid.sogsa.sbs;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;

public class SOGSAClient {
	
	public static void main(String[] args) {
		
		 Client client = new Client(Protocol.HTTP);

	      // The URI of the resource "list of items".
	      Reference itemsUri = new Reference(
	            "http://localhost:8182/semanticbindingservice/items");
	      
	      

	}
	
	public static void createBinding() {
		
		
//		Form form = new Form();
//	      form.add("name", item.getName());
//	      form.add("description", item.getDescription());
//	      Representation rep = form.getWebRepresentation();
		
	}

}
