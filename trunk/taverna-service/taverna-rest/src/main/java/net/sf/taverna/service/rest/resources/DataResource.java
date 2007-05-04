package net.sf.taverna.service.rest.resources;

import java.util.Map.Entry;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.dao.DataDocDAO;
import net.sf.taverna.service.interfaces.ParseException;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class DataResource extends AbstractResource {

	private static Logger logger = Logger.getLogger(DataResource.class);

	DataDocDAO dataDocDao = daoFactory.getDataDocDAO();

	private DataDoc dataDoc;

	public DataResource(Context context, Request request, Response response) {
		super(context, request, response);
		String data_id = (String) request.getAttributes().get("data");
		dataDoc = dataDocDao.read(data_id);
		checkEntity(dataDoc);
	}

	@Override
	public String representPlainText() {
		StringBuffer sb = new StringBuffer();
		sb.append("Data document ");
		sb.append(dataDoc.getId());
		sb.append("\n");
		try {
			for (Entry<String, DataThing> entry : dataDoc.getDataMap().entrySet()) {
				sb.append(entry.getKey()).append(":\n");
				sb.append(entry.getValue());
				sb.append("\n\n");
			}
		} catch (ParseException e) {
			logger.warn("Can't parse data document " + dataDoc, e);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Invalid data document");
		}
		return sb.toString();
	}
	
	@Override
	public String representXML() {
		return dataDoc.getBaclava();
	}
	
}
