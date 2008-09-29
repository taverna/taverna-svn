package net.sf.taverna.service.rest.resources;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.UserDAO;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public abstract class AbstractUserResource extends AbstractResource {

	User user;

	public AbstractUserResource(Context context, Request request,
		Response response) {
		super(context, request, response);
		UserDAO dao = daoFactory.getUserDAO();
		String userName = (String) request.getAttributes().get("user");
		user = dao.readByUsername(userName);
		checkEntity(user);
	}

}
