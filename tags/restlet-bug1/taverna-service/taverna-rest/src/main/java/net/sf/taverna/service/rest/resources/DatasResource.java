package net.sf.taverna.service.rest.resources;

import static net.sf.taverna.service.rest.utils.XMLBeansUtils.xmlOptions;

import java.io.IOException;
import java.util.Iterator;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.dao.DataDocDAO;
import net.sf.taverna.service.xml.Datas;
import net.sf.taverna.service.xml.DatasDocument;

import org.apache.log4j.Logger;
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
		addRepresentation(new URIList());
		addRepresentation(new XML());
	}

	class URIList extends AbstractURIList<DataDoc> {
		@Override
		public Iterator<DataDoc> iterator() {
			return dao.iterator();
		}
	}

	class XML extends AbstractREST {
		@Override
		public DatasDocument getXML() {
			DatasDocument doc = DatasDocument.Factory.newInstance(xmlOptions);
			Datas datas = doc.addNewDatas();
			for (DataDoc dataDoc : dao) {
				datas.addNewData().setHref(uriFactory.getURI(dataDoc));
			}
			return doc;
		}

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
		if (!restType.includes(entity.getMediaType())) {
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
