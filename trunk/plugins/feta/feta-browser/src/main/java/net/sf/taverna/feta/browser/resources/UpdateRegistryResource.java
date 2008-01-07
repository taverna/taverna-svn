package net.sf.taverna.feta.browser.resources;

import java.io.IOException;
import java.net.MalformedURLException;

import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Variant;

public class UpdateRegistryResource extends AbstractResource {

	public UpdateRegistryResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	@Override
	public void handleGet() {
		
		try {
			serviceRegistry.updateFeta();
			getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
			return;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
	}
	
}
