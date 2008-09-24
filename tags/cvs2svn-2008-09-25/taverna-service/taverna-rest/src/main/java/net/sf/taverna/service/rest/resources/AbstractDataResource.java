package net.sf.taverna.service.rest.resources;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.dao.DataDocDAO;

public abstract class AbstractDataResource extends
		AbstractOwnedResource<DataDoc> {

	DataDocDAO dataDocDao = daoFactory.getDataDocDAO();
	
	protected DataDoc dataDoc;

	public AbstractDataResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		String data_id = (String) request.getAttributes().get("data");
		dataDoc = dataDocDao.read(data_id);
		checkEntity(dataDoc);
	}

}