package net.sf.taverna.service.rest.client;

import java.io.IOException;
import java.io.InputStream;

import net.sf.taverna.service.xml.Data;

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
			Response response = getBaclavaResponse();
			return response.getEntity().getText();
		}

		private Response getBaclavaResponse() throws NotSuccessException, MediaTypeException {
			Response response = context.get(getURIReference(), RESTContext.baclavaType);
			return response;
		}
		
		public InputStream getBaclavaStream() throws NotSuccessException, MediaTypeException, IOException {
			Response response = getBaclavaResponse();
			return response.getEntity().getStream();
		}
}
