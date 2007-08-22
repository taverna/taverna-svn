package net.sf.taverna.service.rest.resources;

import static net.sf.taverna.service.rest.utils.XMLBeansUtils.xmlOptions;

import java.io.IOException;
import java.util.Iterator;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.dao.DataDocDAO;
import net.sf.taverna.service.xml.Datas;
import net.sf.taverna.service.xml.DatasDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
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
		addRepresentation(new Velocity());
	}

	class URIList extends AbstractURIList<DataDoc> {
		@Override
		public Iterator<DataDoc> iterator() {
			return user.getDatas().iterator();
		}
	}

	class XML extends AbstractREST<Datas> {
		@Override
		public XmlObject createDocument() {
			DatasDocument doc = DatasDocument.Factory.newInstance(xmlOptions);
			element = doc.addNewDatas();
			return doc;
		}

		@Override
		public void addElements(Datas datas) {
			for (DataDoc dataDoc : user.getDatas()) {
				datas.addNewData().setHref(uriFactory.getURI(dataDoc));
			}
		}
	}
	
	class Velocity extends OwnedVelocity {
		public Velocity() {
			super(user.getDatas(), "Data documents");
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
		if (!baclavaType.includes(entity.getMediaType())) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
				"Content type must be " + baclavaType);
			return;
		}
		DataDoc dataDoc = new DataDoc();
		if (overMaxSize(entity)) {
			logger.warn("Uploaded datadoc was too large");
			return;
		}
		try {
			String baclava = entity.getText();
			if (baclava == null || baclava.equals("")) {
				logger.warn("Missing or empty baclava document");
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Baclava document missing or empty");
				return;
			}
			dataDoc.setBaclava(baclava);
		} catch (IOException e) {
			logger.warn("Could not receive baclava document", e);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			return;
		}
		dataDoc.setOwner(user);
		dao.create(dataDoc);
		daoFactory.commit();
		String uri = uriFactory.getURI(dataDoc);
		logger.info("Created " + dataDoc);
		getResponse().setRedirectRef(uri);
		getResponse().setStatus(Status.SUCCESS_CREATED);

	}

}
