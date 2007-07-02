package net.sf.taverna.service.rest.resources.representation;


import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.ext.velocity.TemplateRepresentation;
import org.restlet.resource.Representation;

public abstract class VelocityRepresentation extends AbstractRepresentation {

	private static String resourcePath;
	private String templateName;
	
	public static void setResourcePath(String path)
	{
		resourcePath=path;
	}
	
	public VelocityRepresentation(String templateName) {
		this.templateName=templateName;
	}
	
	@Override
	public MediaType getMediaType() {
		return MediaType.TEXT_HTML;
	}

	@Override
	public Representation getRepresentation() {
		Map<String,Object> dataModel = getDataModel();
		TemplateRepresentation result = new TemplateRepresentation(templateName,MediaType.TEXT_HTML);
		result.getEngine().setProperty("file.resource.loader.path", resourcePath);
		if (dataModel != null) result.setDataModel(dataModel);
		return result;
	}
	
	protected abstract Map<String,Object> getDataModel();
}
