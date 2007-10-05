package net.sf.taverna.service.rest.client;

import java.io.IOException;
import java.io.InputStream;

import net.sf.taverna.service.xml.Data;
import net.sf.taverna.service.xml.DataDocument;
import net.sf.taverna.service.xml.User;

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
		
		/**
		 * Set the owner of the resource, or set to <code>null</code> if the
		 * resource is to have no owner.
		 * 
		 * @param owner The {@link UserREST} which is to be the owner
		 * @throws NotSuccessException
		 */
		public void setOwner(UserREST owner) throws NotSuccessException {
			DataDocument dataDoc = DataDocument.Factory.newInstance();
			User ownerElem = dataDoc.addNewData().addNewOwner();
			if (owner != null) {
				ownerElem.setHref(owner.getURI());
			}
			context.put(getURIReference(), dataDoc);
			invalidate();
		}
		
		public DataREST clone() {
			return new DataREST(context, getURIReference());
		}
}
