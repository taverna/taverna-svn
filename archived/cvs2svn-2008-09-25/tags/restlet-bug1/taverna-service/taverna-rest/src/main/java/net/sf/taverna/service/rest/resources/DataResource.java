package net.sf.taverna.service.rest.resources;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.dao.DataDocDAO;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class DataResource extends AbstractResource {

	private static Logger logger = Logger.getLogger(DataResource.class);

	DataDocDAO dataDocDao = daoFactory.getDataDocDAO();

	private DataDoc dataDoc;

	public DataResource(Context context, Request request, Response response) {
		super(context, request, response);
		String data_id = (String) request.getAttributes().get("data");
		dataDoc = dataDocDao.read(data_id);
		checkEntity(dataDoc);
		addRepresentation(new Baclava());
	}

	class Baclava extends AbstractText {
		@Override
		public String getText() {
			return dataDoc.getBaclava();
		}

		@Override
		public MediaType getMediaType() {
			return baclavaType;
		}
	}

}
