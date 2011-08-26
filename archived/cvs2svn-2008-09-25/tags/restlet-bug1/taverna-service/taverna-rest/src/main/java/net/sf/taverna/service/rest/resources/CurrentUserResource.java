package net.sf.taverna.service.rest.resources;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.rest.UserGuard;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class CurrentUserResource extends Resource {
	private static Logger logger = Logger.getLogger(CurrentUserResource.class);

	private static URIFactory uriFactory = URIFactory.getInstance();

	@Override
	public Representation getRepresentation(Variant variant) {
		User user =
			(User) getContext().getAttributes().get(
				UserGuard.AUTHENTICATED_USER);
		getResponse().redirectTemporary(uriFactory.getURI(user));
		return new StringRepresentation(user.getUsername(),
			MediaType.TEXT_PLAIN);
	}

}
