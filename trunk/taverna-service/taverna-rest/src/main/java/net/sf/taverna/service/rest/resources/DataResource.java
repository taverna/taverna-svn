package net.sf.taverna.service.rest.resources;

import java.io.IOException;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DataDocDAO;
import net.sf.taverna.service.xml.Data;
import net.sf.taverna.service.xml.DataDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

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
		addRepresentation(new XML());
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

	class XML extends AbstractXML {

		@Override
		public XmlObject getXML() {
			DataDocument doc = DataDocument.Factory.newInstance();
			doc.addNewData();
			if (dataDoc.getOwner() != null) {
				doc.getData().addNewOwner().setHref(
					uriFactory.getURI(dataDoc.getOwner()));
			}
			XmlObject xml;
			try {
				xml = XmlObject.Factory.parse(dataDoc.getBaclava());
				doc.getData().addNewBaclava().set(xml);
			} catch (XmlException e) {
				logger.warn("Could not parse baclava for " + dataDoc, e);
			}
			return doc;
		}
	}
	

	@Override
	public long maxSize() {
		return DataDoc.BACLAVA_MAX;
	}

	@Override
	public boolean allowPut() {
		return true;
	}
	
	@Override
	public void put(Representation entity) {
		if (!restType.includes(entity.getMediaType())) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
				"Content type must be " + restType);
			return;
		}
		if (overMaxSize(entity)) {
			logger.warn("Uploaded datadoc was too large");
			return;
		}
		DataDocument dataDocument;
		try {
			dataDocument = DataDocument.Factory.parse(entity.getStream());
		} catch (XmlException e) {
			logger.warn("Could not parse XML", e);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
				"Could not parse XML");
			return;
		} catch (IOException e) {
			logger.warn("Could not read XML", e);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
				"Could not read XML");
			return;
		}
		Data data = dataDocument.getData();
		if (data.getOwner() != null) {
			String ownerURI = data.getOwner().getHref();
			User owner = null;
			if (ownerURI != null) {
				owner = uriToDAO.getResource(ownerURI, User.class);
			}
			dataDoc.setOwner(owner);
		}
		if (data.getBaclava() != null) {
			dataDoc.setBaclava(data.getBaclava().xmlText());
		}
		daoFactory.commit();
		logger.info("Updated " + dataDoc);
		getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
	}

}
