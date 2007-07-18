package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Configuration;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Resource responsible for user registration. Its based around a Velocity template named register.vm
 * 
 * @author sowen
 *
 */
public class UserRegisterResource extends AbstractUserCreationResource {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(UserRegisterResource.class);

	public UserRegisterResource(Context context, Request request, Response response) {
		super(context, request, response);
	}
	
	protected VelocityRepresentation getVelocityRepresentation() {
		return new RegisterVelocityRepresentation();
	}
	
	

	protected VelocityRepresentation getVelocityRepresentationForError(String name, String password, String email, String confirm, Exception e) {
		return new RegisterVelocityRepresentation(name,password,confirm,email,e.getMessage());
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
			Configuration config=DAOFactory.getFactory().getConfigurationDAO().getConfig();
			model.put("allowRegister", config.isAllowRegister());
			return model;
		}
	}
}
