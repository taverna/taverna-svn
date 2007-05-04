package net.sf.taverna.service.rest.resources;

import java.io.IOException;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.dao.DataDocDAO;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

public class DatasResource extends AbstractUserResource {
	
	private static Logger logger = Logger.getLogger(DatasResource.class);
	
	private DataDocDAO dao = daoFactory.getDataDocDAO();
	
	public DatasResource(Context context, Request request, Response response) {
		super(context, request, response);
	}

	@Override
	public String representPlainText() {
    	StringBuilder message = new StringBuilder();
    	for (DataDoc dataDoc : dao) {
    		message.append(dataDoc.getId()).append("\n");
    	}
    	return message.toString();
	}

	@Override
	public Element representXMLElement() {
		Element datasElement = new Element("data", ns);
		datasElement.addNamespaceDeclaration(URIFactory.NS_XLINK);
		for (DataDoc dataDoc : dao) {
			Element docElement = new Element("datadoc", ns);
			docElement.setAttribute(uriFactory.getXLink(dataDoc));
			datasElement.addContent(docElement);
		}
		return datasElement;
	}
	
	@Override
	public boolean allowPost() {
		return true;
	}
	
	@Override
	public long maxSize() {
		return DataDoc.BACLAVA_MAX;
	}
	
	@Override
	public void post(Representation entity) {
		if (! restType.includes(entity.getMediaType())) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, 
				"Content type must be " + restType);
			return;
		}
		DataDoc dataDoc = new DataDoc();
		if (overMaxSize(entity)) {
			logger.warn("Uploaded datadoc was too large");
			return;
		}
		try {
			dataDoc.setBaclava(entity.getText());
		} catch (IOException e) {
			logger.warn("Could not receive baclava document", e);
		}
		dao.create(dataDoc);
		daoFactory.commit();
		getResponse().setRedirectRef("/data/" + dataDoc.getId());
		getResponse().setStatus(Status.SUCCESS_CREATED);
		
	}

	
}
