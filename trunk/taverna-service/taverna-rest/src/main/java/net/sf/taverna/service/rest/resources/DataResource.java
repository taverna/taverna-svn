package net.sf.taverna.service.rest.resources;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.dao.DataDocDAO;
import net.sf.taverna.service.xml.DataDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
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
			doc.addNewData().addNewOwner().setHref(uriFactory.getURI(dataDoc.getOwner()));
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

}
