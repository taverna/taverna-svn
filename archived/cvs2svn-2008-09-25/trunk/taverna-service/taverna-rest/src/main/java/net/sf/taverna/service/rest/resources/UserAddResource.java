package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Configuration;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.UserGuard;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class UserAddResource extends AbstractUserCreationResource {
	public UserAddResource(Context context, Request request, Response response) {
		super(context, request, response);
	}
		
	@Override
	protected VelocityRepresentation getVelocityRepresentation() {
		return new AddUserVelocityRepresentation();
	}

	@Override
	protected VelocityRepresentation getVelocityRepresentationForError(Form form, Exception e) {
		return new AddUserVelocityRepresentation(form.getValues("name"),form.getValues("password"),form.getValues("confirm"),form.getValues("email"),e.getMessage());
	}

	class AddUserVelocityRepresentation extends VelocityRepresentation {

		private Map<String,Object> model = new HashMap<String, Object>();
		
		public AddUserVelocityRepresentation() {
			User user = getAuthUser();
			model.put("isAdmin",user!=null && user.isAdmin());
		}
		
		public AddUserVelocityRepresentation(String name,String password, String confirm, String email,String errorMsg) {
			this();
			model.put("name", name);
			model.put("password",password);
			model.put("confirm", confirm);
			model.put("email", email);
			model.put("errorMsg", errorMsg);
		}
		
		@Override
		protected String pageTitle() {
			return "Add a new user";
		}

		@Override
		protected String templateName() {
			return "adduser.vm";
		}

		@Override
		protected Map<String, Object> getDataModel() {
			Configuration config=DAOFactory.getFactory().getConfigurationDAO().getConfig();
			model.put("allowRegister", config.isAllowRegister());
			return model;
		}
	}

	@Override
	public boolean userCreationAllowed() {
		return checkIsAdmin();
	}
	
}
