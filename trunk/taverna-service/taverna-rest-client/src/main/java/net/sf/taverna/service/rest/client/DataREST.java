package net.sf.taverna.service.rest.client;

import java.io.IOException;

import net.sf.taverna.service.xml.Data;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Response;

public class DataREST extends OwnedREST<Data> {

		public DataREST(RESTContext context, Data document) {
			super(context, document);
		}
		
		public DataREST(RESTContext context, Reference uri, Data document) {
			super(context, uri, document);
		}
		
		public DataREST(RESTContext context, Reference uri) {
			super(context, uri, Data.class);
		}
		
		public String getBaclava() throws NotSuccessException, MediaTypeException, IOException {
			Response response = context.get(getURIReference(), context.baclavaType);
			return response.getEntity().getText();
		}
}
