package net.sf.taverna.feta.browser.resources;

import net.sf.taverna.feta.browser.elmo.ServiceRegistry;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

public class RegistryUpdateResource extends Resource {

	ServiceRegistry serviceRegistry = ServiceRegistry.getInstance();

	public RegistryUpdateResource(Context context, Request request,
			Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	@Override
	public void handleGet() {

		try {
			serviceRegistry.updateFeta();
			getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
			return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
	}

}
