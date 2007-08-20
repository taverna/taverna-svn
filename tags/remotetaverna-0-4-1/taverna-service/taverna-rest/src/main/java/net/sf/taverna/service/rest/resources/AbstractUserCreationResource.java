package net.sf.taverna.service.rest.resources;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.UserDAO;
import net.sf.taverna.service.rest.UserGuard;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;
import net.sf.taverna.service.rest.resources.util.UserDetailsValidator;
import net.sf.taverna.service.rest.resources.util.UserValidationException;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

public abstract class AbstractUserCreationResource extends AbstractResource {
	
	private static Logger logger = Logger
			.getLogger(AbstractUserCreationResource.class);
	
	public AbstractUserCreationResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(getVelocityRepresentation());
	}
	
	@Override
	public boolean allowPost() {
		return true;
	}
	
	@Override
	public void post(Representation entity) {
		
		if (! userCreationAllowed()) {
			return;
		}
				
		if (!entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,"The media type must be of type:"+MediaType.APPLICATION_WWW_FORM);
			logger.warn("Unsupported media type, "+entity.getMediaType()+", not supported");
		}
		else {
			Form form = new Form(entity);
			processForm(form); 
		}
	}

	public abstract boolean userCreationAllowed();
	
	protected void processForm(Form form) {
		String name = form.getValues("name");
		String password = form.getValues("password");
		String email = form.getValues("email");
		String confirm = form.getValues("confirm");
		try {
			validate(name,password,confirm,email);
			User user=createUser(name,password,email);
			if (getRequest().getAttributes().containsKey(
				UserGuard.AUTHENTICATED_USER)) {
				getResponse().redirectSeeOther(
					URIFactory.getInstance().getURI(user));
			} else {
				// Avoid popping up password prompt, go to /v1 instead
				getResponse().redirectSeeOther(uriFactory.getApplicationRoot());
			}
		} catch (Exception e) {
			getResponse().setEntity(getVelocityRepresentationForError(form, e).getRepresentation(getRequest(),getResponse()));
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
		}
	}
	
	public void validate(String name,String password,String confirm, String email) throws UserValidationException
	{
		UserDetailsValidator.validate(name, password, confirm, email);
	}
	
	protected User createUser(String name, String password, String email) throws CreateUserException {
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
	
	protected abstract VelocityRepresentation getVelocityRepresentation();
	protected abstract VelocityRepresentation getVelocityRepresentationForError(Form form, Exception e);
	
	@SuppressWarnings("serial")
	class CreateUserException extends Exception {
		public CreateUserException(String message) {
			super(message);
		}
	}
}
