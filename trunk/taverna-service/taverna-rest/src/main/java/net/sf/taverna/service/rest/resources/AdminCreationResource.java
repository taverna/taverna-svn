package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.UserDAO;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class AdminCreationResource extends AbstractUserCreationResource {

	public AdminCreationResource(Context context, Request request,
			Response response) {
		super(context, request, response);
	}

	@Override
	protected VelocityRepresentation getVelocityRepresentation() {
		return new AdminCreationRepresentation();
	}

	@Override
	protected VelocityRepresentation getVelocityRepresentationForError(
			String name, String password, String email, String confirm,
			Exception e) {
		return new AdminCreationRepresentation(name,password,confirm,email,e.getMessage());
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
	
	class AdminCreationRepresentation extends VelocityRepresentation {
		
		private Map<String,Object> model = new HashMap<String, Object>();
		public AdminCreationRepresentation() {
			
		}
		public AdminCreationRepresentation(String name,String password,String confirm,String email,String errorMsg) {
			model.put("name", name);
			model.put("password",password);
			model.put("confirm", confirm);
			model.put("email", email);
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
