package net.sf.taverna.service.rest.resources.representation;

import java.util.Date;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Configuration;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.UserGuard;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.velocity.TemplateRepresentation;
import org.restlet.resource.Representation;

public abstract class VelocityRepresentation extends AbstractRepresentation {

	private static Logger logger =
		Logger.getLogger(VelocityRepresentation.class);

	private static String resourcePath;

	public static void setResourcePath(String path) {
		if (path == null) {
			throw new NullPointerException("Resource path can't be null");
		}
		logger.info("Using velocity resource loader path of :" + path);
		resourcePath = path;
	}

	@Override
	public MediaType getMediaType() {
		return MediaType.TEXT_HTML;
	}

	@Override
	public Representation getRepresentation(Request request,Response response) {
		Map<String, Object> dataModel = getDataModel();
		dataModel.put("page_template", templateName());
		dataModel.put("page_title", pageTitle());
		TemplateRepresentation result =
			new TemplateRepresentation("layout.vm", MediaType.TEXT_HTML);
		if (resourcePath == null) {
			logger.warn("Velocity resource path has not been set");
			return null;
		}
		result.getEngine().setProperty("file.resource.loader.path",
			resourcePath);
	
		URIFactory uriFactory = URIFactory.getInstance();
		if (request.getAttributes().get(UserGuard.AUTHENTICATED_USER)!=null) {
			dataModel.put("authuser", request.getAttributes().get(UserGuard.AUTHENTICATED_USER));
			dataModel.put("config_uri", uriFactory.getURI(Configuration.class));
			dataModel.put("queue_uri", uriFactory.getURIDefaultQueue());
			dataModel.put("workers_uri",uriFactory.getURI(Worker.class));
			dataModel.put("user_uri",uriFactory.getURICurrentUser());
			dataModel.put("adduser_uri", uriFactory.getURIAddUser());
		}
		else {
			if (DAOFactory.getFactory().getConfigurationDAO().getConfig().isAllowRegister()) {
				dataModel.put("register_uri", uriFactory.getURIRegister());
			}
		}
		
		result.setDataModel(dataModel);
		
		if (isDynamic()) {
			result.setModificationDate(new Date());
			result.setExpirationDate(result.getModificationDate());
		}
		
		return result;
	}

	public boolean isDynamic() {
		return false;
	}

	protected abstract Map<String, Object> getDataModel();
	protected abstract String pageTitle();
	protected abstract String templateName();
}
