package net.sf.taverna.feta.browser.util;

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.ext.velocity.TemplateRepresentation;

public class VelocityRepresentation extends TemplateRepresentation {

	private static final String templateDir = "templates/";

	public VelocityRepresentation(String templateName,
			Map<String, Object> dataModel, MediaType mediaType) {
		super(templateDir + templateName, dataModel, mediaType);
		configureEngine();
	}

	public VelocityRepresentation(String templateName, MediaType mediaType) {
		super(templateDir + templateName, mediaType);
		configureEngine();
	}

	protected void configureEngine() {
		getEngine().setProperty("runtime.log.logsystem",
				"org.apache.velocity.runtime.log.Log4JLogChute");
		getEngine().setProperty("resource.loader", "class");
		getEngine()
				.setProperty("class.resource.loader.class",
						"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
	}

}
