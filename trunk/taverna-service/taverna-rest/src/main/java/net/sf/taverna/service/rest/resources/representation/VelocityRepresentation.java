package net.sf.taverna.service.rest.resources.representation;

import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
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
	public Representation getRepresentation() {
		Map<String, Object> dataModel = getDataModel();
		dataModel.put("page_template", templateName());
		dataModel.put("page_title",pageTitle());
		TemplateRepresentation result =
			new TemplateRepresentation("layout.vm", MediaType.TEXT_HTML);
		if (resourcePath == null) {
			logger.warn("Velocity resource path has not been set");
			return null;
		}
		result.getEngine().setProperty("file.resource.loader.path",
			resourcePath);
		if (dataModel != null)
			result.setDataModel(dataModel);
		return result;
	}

	protected abstract Map<String, Object> getDataModel();
	protected abstract String pageTitle();
	protected abstract String templateName();
}
