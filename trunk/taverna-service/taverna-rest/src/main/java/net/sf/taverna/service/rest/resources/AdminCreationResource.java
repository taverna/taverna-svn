package net.sf.taverna.service.rest.resources;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Configuration;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.UserDAO;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class AdminCreationResource extends AbstractUserCreationResource {

	public AdminCreationResource(Context context, Request request,
			Response response) {
		super(context, request, response);
	}

	@Override
	protected VelocityRepresentation getVelocityRepresentation() {
		return new AdminCreationRepresentation();
	}

	
	protected VelocityRepresentation getVelocityRepresentationForError(Form form, Exception e) {
		return new AdminCreationRepresentation(form.getValues("name"), form.getValues("password"),form.getValues("confirm"),form.getValues("email"),form.getValues("baseuri"),e.getMessage());
	}
	
	protected User createUser(String name, String password, String email) throws CreateUserException {
		DAOFactory daoFactory = DAOFactory.getFactory();
		UserDAO userDAO = daoFactory.getUserDAO();
		User user=null;
		
		try {
			user = new User(name);
			user.setPassword(password);
			user.setEmail(email);
			user.setAdmin(true);
			userDAO.create(user);
			daoFactory.commit();
		}
		catch(Throwable e) {
			throw new CreateUserException("An internal error prevented the user beining created. The error was:"+e.getCause().getMessage());
		}
		return user;
	}
	
	
	
	@Override
	protected void processForm(Form form) {
		String baseuri;
		try {
			baseuri=form.getValues("baseuri");
			if (! baseuri.endsWith("/")) {
				baseuri += "/";
			}
			new URL(baseuri); // to test if it's valid
			updateBaseuri(baseuri);
			super.processForm(form);
		}
		catch(MalformedURLException e) {
			getResponse().setEntity(getVelocityRepresentationForError(form, e).getRepresentation());
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
		}
		
	}

	private void updateBaseuri(String url) {
		Configuration config=daoFactory.getConfigurationDAO().getConfig();
		config.setBaseuri(url);
		daoFactory.getConfigurationDAO().update(config);
		URIFactory.BASE_URI_CHANGED=true;
	}


	class AdminCreationRepresentation extends VelocityRepresentation {
		
		private Map<String,Object> model = new HashMap<String, Object>();
		public AdminCreationRepresentation() {
			model.put("baseuri", new Reference(getRequest().getRootRef(), ".").getTargetRef());
		}
		
		public AdminCreationRepresentation(String name,String password,String confirm,String email, String baseuri,String errorMsg) {
			model.put("name", name);
			model.put("password",password);
			model.put("confirm", confirm);
			model.put("email", email);
			model.put("baseuri",baseuri);
			model.put("errorMsg", errorMsg);
		}
		
	
		@Override
		protected String pageTitle() {
			return "Admin registration";
		}

		@Override
		protected String templateName() {
			return "createadmin.vm";
		}



		@Override
		protected Map<String, Object> getDataModel() {
			return model;
		}
	}
	

}
