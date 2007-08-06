package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.Map;

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

public class UserEditResource extends AbstractUserResource {
	
	private static Logger logger = Logger.getLogger(UserEditResource.class);
	
	public UserEditResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(new VelocityEditUserRepresentation());
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
			String email=form.getValues("email");
			String password=form.getValues("password");
			String newPassword=form.getValues("newpassword");
			String newConfirm=form.getValues("newconfirm");
			boolean updatePassword=form.getValues("updatepassword")!=null;
			boolean userIsAdmin=form.getValues("userisadmin")!=null;
			if (email==null) email="";
			if (password==null) password="";
			if (newPassword==null) newPassword="";
			if (newConfirm==null) newConfirm="";
			
			//check that the admin status isn't being changed through spoofing the form post!
			if (userIsAdmin  && !getAuthUser().isAdmin()) {
				logger.warn("A dubious attempt to change admin status on user:"+user.getUsername()+" without being an admin user themself!");
				userIsAdmin=false;
			}
			
			try {
				validate(password,updatePassword,userIsAdmin,email,newPassword,newConfirm);
				updateUser(updatePassword,userIsAdmin,email,newPassword);
				getResponse().setRedirectRef(URIFactory.getInstance().getURI(user));
				getResponse().setStatus(Status.REDIRECTION_FOUND);
			}
			catch(Exception e) {
				logger.warn("Error updating user:");
				getResponse().setEntity(new VelocityEditUserRepresentation(password,updatePassword,userIsAdmin,email,newPassword,newConfirm,e.getMessage()).getRepresentation(getRequest(),getResponse()));
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			}
		}
	}
	
	private void validate(String password,boolean updatePassword,boolean userIsAdmin,String email,String newPassword,String newConfirm) throws UserValidationException {
		if (!getAuthUser().checkPassword(password)) throw new UserValidationException("Incorrect password");
		if (user.equals(getAuthUser()) && user.isAdmin() && userIsAdmin==false) throw new UserValidationException("Demoting yourself from adminstrator is not allowed.");
		if (updatePassword) {
			UserDetailsValidator.validate(newPassword, newConfirm, email);
		}
		else {
			UserDetailsValidator.validateEmail(email);
		}
	}
	
	private void updateUser(boolean updatePassword,boolean userIsAdmin,String email,String newPassword) throws EditUserException {
		user.setAdmin(userIsAdmin);
		user.setEmail(email);
		if (updatePassword) user.setPassword(newPassword);
		daoFactory.getUserDAO().update(user);
		daoFactory.commit();
	}

	class VelocityEditUserRepresentation extends VelocityRepresentation
	{
		private Map<String,Object> model = new HashMap<String, Object>();
		
		public VelocityEditUserRepresentation(String password, boolean updatePassword, boolean userIsAdmin,String email,String newPassword, String newConfirm, String errorMsg) {
			model.put("name",user.getUsername());
			model.put("updatepassword",updatePassword);
			model.put("userisadmin",userIsAdmin);
			model.put("password", password);
			model.put("email",email);
			model.put("errorMsg",errorMsg);
			model.put("newpassword",newPassword);
			model.put("newconfirm",newConfirm);
		}
		
		public VelocityEditUserRepresentation() {
			if (user!=null) {
				model.put("name", user.getUsername());
				model.put("email", user.getEmail());
				model.put("userisadmin", user.isAdmin());
				model.put("updatepassword",false);
			}
			else logger.error("No user found");
		}
		
		@Override
		protected String pageTitle() {
			return "Edit user settings";
		}

		@Override
		protected String templateName() {
			return "edituser.vm";
		}

		@Override
		protected Map<String, Object> getDataModel() {
			model.put("isAdmin", getAuthUser().isAdmin());
			return model;
		}
		
	}
	
	class EditUserException extends Exception {
		
		private static final long serialVersionUID = -3631937452831493684L;

		public EditUserException(String msg) {
			super(msg);
		}
	}
}
