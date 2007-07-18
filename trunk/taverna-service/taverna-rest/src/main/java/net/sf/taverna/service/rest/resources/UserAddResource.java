package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Configuration;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;

import org.restlet.Context;
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
	protected VelocityRepresentation getVelocityRepresentationForError(String name, String password, String email, String confirm, Exception e) {
		return new AddUserVelocityRepresentation(name,password,confirm,email,e.getMessage());
	}


	class AddUserVelocityRepresentation extends VelocityRepresentation {

		private Map<String,Object> model = new HashMap<String, Object>();
		
		public AddUserVelocityRepresentation() {
			super("adduser.vm");
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
		protected Map<String, Object> getDataModel() {
			Configuration config=DAOFactory.getFactory().getConfigurationDAO().getConfig();
			model.put("allowRegister", config.isAllowRegister());
			return model;
		}
	}
	
}
