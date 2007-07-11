package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.UserDAO;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

/**
 * Resource responsible for user registration. Its based around a Velocity template named register.vm
 * 
 * @author sowen
 *
 */
public class UserRegisterResource extends AbstractResource {
	private static Logger logger = Logger.getLogger(UserRegisterResource.class);

	public UserRegisterResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(new RegisterVelocityRepresentation());
	}
	
	@Override
	public boolean allowPost() {
		return true;
	}
	
	@Override
	public void post(Representation entity) {
		
		if (!entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,"The media type must be of type:"+MediaType.APPLICATION_WWW_FORM);
			logger.warn("Unsupported media type, "+entity.getMediaType()+", not supported");
		}
		else {
			Form form = new Form(entity);
			String name = form.getValues("name");
			String password = form.getValues("password");
			String email = form.getValues("email");
			String confirm = form.getValues("confirm");
			try {
				validate(name,password,confirm,email);
				User user=createUser(name,password,email);
				getResponse().setRedirectRef(URIFactory.getInstance(getRequest()).getURI(user));
				getResponse().setStatus(Status.REDIRECTION_FOUND);
			} catch (CreateUserException e) {
				getResponse().setEntity(new RegisterVelocityRepresentation(name,password,form.getValues("confirm"),email,e.getMessage()).getRepresentation());
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			}
		}
	}
	
	private void validate(String name,String password,String confirm, String email) throws CreateUserException
	{
		if (name==null) throw new CreateUserException("You must provide a username");
		if (!isAlphaNumeric(name)) throw new CreateUserException("The username must contain only alpha numeric characters and no spaces");
		if (password==null) throw new CreateUserException("You must provide a password");
		if (confirm==null) throw new CreateUserException("You must confirm the passowrd");
		if (email==null) throw new CreateUserException("You must provide a valid email address");
		if (!password.equals(confirm)) throw new CreateUserException("The confirmation password does not match");
		if (!email.contains("@")) throw new CreateUserException("The email address is invalid");
		
		UserDAO userDAO = DAOFactory.getFactory().getUserDAO();
		if (userDAO.readByUsername(name)!=null) throw new CreateUserException("The username '"+name+"' has already been taken.");
	}

	private boolean isAlphaNumeric(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isLetterOrDigit(c)) return false;
		}
		return true;
	}
	
	private User createUser(String name, String password, String email) throws CreateUserException {
		DAOFactory daoFactory = DAOFactory.getFactory();
		UserDAO userDAO = daoFactory.getUserDAO();
		User user=null;
		
		try {
			user = new User(name);
			user.setPassword(password);
			user.setEmail(email);
			userDAO.create(user);
			daoFactory.commit();
		}
		catch(Throwable e) {
			throw new CreateUserException("An internal error prevented the user beining created. The error was:"+e.getCause().getMessage());
		}
		return user;
	}


	class RegisterVelocityRepresentation extends VelocityRepresentation {

		private Map<String,Object> model = new HashMap<String, Object>();
		
		public RegisterVelocityRepresentation() {
			super("register.vm");
		}
		
		public RegisterVelocityRepresentation(String name,String password, String confirm, String email,String errorMsg) {
			super("register.vm");
			model.put("name", name);
			model.put("password",password);
			model.put("confirm", confirm);
			model.put("email", email);
			model.put("errorMsg", errorMsg);
		}
		
		@Override
		protected Map<String, Object> getDataModel() {
			return model;
		}
	}
	
	@SuppressWarnings("serial")
	class CreateUserException extends Exception {
		public CreateUserException(String message) {
			super(message);
		}
	}
}
