package net.sf.taverna.service.rest.resources;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Configuration;
import net.sf.taverna.service.datastore.dao.DAOFactory;
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

public class ConfigurationResource extends AbstractResource {
	
	private static Logger logger = Logger
			.getLogger(ConfigurationResource.class);

	public ConfigurationResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(new ConfigVelocityRepresentation());
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
			boolean allowRegister=form.getValues("allowregister")!=null;
			boolean allowEmail=form.getValues("allowemail")!=null;
			String smtpServer=form.getValues("smtpserver");
			String username=form.getValues("smtpusername");
			String password=form.getValues("smtppassword");
			String confirm=form.getValues("smtppasswordconfirm");
			if (password==null) password="";
			if (confirm==null) confirm="";
			if (username==null) username="";
			if (smtpServer==null) smtpServer="";
			boolean smtpAuthRequired=form.getValues("smtpauthrequired")!=null;
			
			try {
				validate(allowRegister,allowEmail,smtpServer,smtpAuthRequired,username,password,confirm);
				Configuration config=updateConfig(allowRegister,allowEmail,smtpServer,smtpAuthRequired,username,password);
				if (logger.isDebugEnabled()) logger.debug("Successfully updated the configuration");
				getResponse().setRedirectRef(URIFactory.getInstance(getRequest()).getURI(config));
				getResponse().setStatus(Status.REDIRECTION_FOUND);
			}
			catch(ConfigurationUpdateException e) {
				logger.warn("Unable to update configuration:",e);
				getResponse().setEntity(new ConfigVelocityRepresentation(allowRegister,allowEmail,smtpServer,smtpAuthRequired,username,password,confirm,e.getMessage()).getRepresentation());
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			}
		}
	}
	
	private Configuration updateConfig(boolean allowRegister, boolean allowEmail, String smtpServer, boolean smtpAuthRequired,String username,String password) throws ConfigurationUpdateException {
		Configuration config = DAOFactory.getFactory().getConfigurationDAO().getConfig();
		if (config==null) throw new ConfigurationUpdateException("Unable to find configuration database record");
		try {
			config.setAllowRegister(allowRegister);
			config.setAllowEmailNotifications(allowEmail);
			config.setSmtpAuthRequired(smtpAuthRequired);
			if (allowEmail) {
				config.setSmtpServer(smtpServer);
				if (smtpAuthRequired) {
					config.setSmtpUser(username);
					config.setSmtpPassword(password);
				}
			}
			DAOFactory.getFactory().getConfigurationDAO().update(config);
			DAOFactory.getFactory().commit();
		}
		catch(Throwable e) {
			logger.error("Error updating configuration",e);
			throw new ConfigurationUpdateException("An internal error occurred trying to update the configuration:"+e.getMessage());
		}
		return config;
	}
	
	private void validate(boolean allowRegister, boolean allowEmail, String smtpServer, boolean smtpAuthRequired,String username,String password, String confirmPassword) throws ConfigurationUpdateException {
		if (allowEmail && smtpServer.length()<=0) {
			throw new ConfigurationUpdateException("You must provide an SMTP server name");
		}
		
		if (smtpAuthRequired) {
			if (username.length()<=0) throw new ConfigurationUpdateException("You must provide a username if you required SMTP authentication");
			if (!password.equals(confirmPassword)) throw new ConfigurationUpdateException("The password does not match the confirmation password");
		}
	}

	class ConfigVelocityRepresentation extends VelocityRepresentation {
		
		private Map<String,Object> model = new HashMap<String, Object>();
		private static final String template="adminconfig.vm";
		
		public ConfigVelocityRepresentation(boolean allowRegister, boolean allowEmail, String smtpServer, boolean smtpAuthRequired,String username,String password, String confirmPassword,String errorMsg) {
			super(template);
			model.put("allowregister",allowRegister);
			model.put("allowemail", allowEmail);
			model.put("smtpserver", smtpServer);
			model.put("smtpusername", username);
			model.put("smtppassword",password);
			model.put("smtppasswordconfirm",confirmPassword);
			model.put("smtpauthrequired", smtpAuthRequired);
			model.put("errorMsg",errorMsg);
		}
		
		public ConfigVelocityRepresentation() {
			super(template);
			Configuration config = DAOFactory.getFactory().getConfigurationDAO().getConfig();
			model.put("allowregister",config.isAllowRegister());
			model.put("allowemail", config.isAllowEmailNotifications());
			model.put("smtpserver", config.getSmtpServer());
			model.put("smtpusername", config.getSmtpUser());
			model.put("smtppassword",config.getSmtpPassword());
			model.put("smtppasswordconfirm",config.getSmtpPassword());
			model.put("smtpauthrequired", config.isSmtpAuthRequired());
		}

		@Override
		protected Map<String, Object> getDataModel() {
			model.put("currentuser", getAuthUser());
			return model;
		}
	}
	
	@SuppressWarnings("serial")
	class ConfigurationUpdateException extends Exception {
		public ConfigurationUpdateException(String msg) {
			super(msg);
		}
	}
}
