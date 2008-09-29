package net.sf.taverna.service.rest.resources;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.rest.UserGuard;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.restlet.Context;
import org.restlet.data.Dimension;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

import com.noelios.restlet.http.HttpConstants;

public class CurrentUserResource extends Resource {	
	private URIFactory uriFactory;

	public CurrentUserResource(Context context, Request req, Response response) {
		super(context, req, response);
		uriFactory = URIFactory.getInstance();
	}

	@Override
	public void handleGet() {
		// Set headers to indicate that this redirection is only valid with
		// current Authorization

		Form additionalHeaders = new Form();
		additionalHeaders.add("Cache-Control", "private");

		// FIXME: Should be able to do Vary: Authorization instead of *
		//additionalHeaders.add("Vary", "Authorization");
		getResponse().getDimensions().add(Dimension.UNSPECIFIED);

		getResponse().getAttributes().put(HttpConstants.ATTRIBUTE_HEADERS,
			additionalHeaders);

		User user =
			(User) getRequest().getAttributes().get(
				UserGuard.AUTHENTICATED_USER);
		getResponse().redirectTemporary(uriFactory.getURIUser(user));
	}
}
