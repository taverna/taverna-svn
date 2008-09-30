package net.sf.taverna.service.rest.resources;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
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
			String fromEmail=form.getValues("fromemailaddress");
			String baseuri=form.getValues("baseuri");
			String workermemory=form.getValues("workermemory");
			String tavernahome=form.getValues("tavernahome");
			if (password==null) password="";
			if (confirm==null) confirm="";
			if (username==null) username="";
			if (smtpServer==null) smtpServer="";
			if (fromEmail==null) fromEmail="";
			if (baseuri==null) baseuri="";
			if (workermemory==null) workermemory="500";
			if (tavernahome==null) tavernahome="";
			tavernahome=new File(tavernahome).getAbsolutePath();
			
			boolean smtpAuthRequired=form.getValues("smtpauthrequired")!=null;
			
			try {
				validate(allowRegister,allowEmail,smtpServer,fromEmail,smtpAuthRequired,username,password,confirm,baseuri,workermemory,tavernahome);
				updateConfig(allowRegister,allowEmail,smtpServer,fromEmail,smtpAuthRequired,username,password,baseuri,workermemory,tavernahome);
				if (logger.isDebugEnabled()) logger.debug("Successfully updated the configuration");
				getResponse().setEntity(new ConfigVelocityRepresentation("Successfully updated").getRepresentation(getRequest(),getResponse()));
			}
			catch(ConfigurationUpdateException e) {
				logger.warn("Unable to update configuration:",e);
				getResponse().setEntity(new ConfigVelocityRepresentation(allowRegister,allowEmail,smtpServer,fromEmail,smtpAuthRequired,username,password,confirm,baseuri,workermemory,tavernahome,e.getMessage()).getRepresentation(getRequest(),getResponse()));
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			}
		}
	}
	
	private void updateConfig(boolean allowRegister, boolean allowEmail, String smtpServer,String fromEmail, boolean smtpAuthRequired,String username,String password,String baseuri,String workermemory,String tavernahome) throws ConfigurationUpdateException {
		DAOFactory daoFactory = DAOFactory.getFactory();
		Configuration config = daoFactory.getConfigurationDAO().getConfig();
		if (config==null) throw new ConfigurationUpdateException("Unable to find configuration database record");
		try {
			config.setAllowRegister(allowRegister);
			config.setAllowEmailNotifications(allowEmail);
			config.setSmtpAuthRequired(smtpAuthRequired);
			if (allowEmail) {
				config.setSmtpServer(smtpServer);
				config.setFromEmail(fromEmail);
				if (smtpAuthRequired) {
					config.setSmtpUser(username);
					config.setSmtpPassword(password);
				}
			}
			config.setBaseuri(baseuri);
			config.setWorkerMemory(workermemory);
			config.setTavernaHome(tavernahome);
			URIFactory.BASE_URI_CHANGED=true;
			daoFactory.getConfigurationDAO().update(config);
			daoFactory.commit();
		}
		catch(Throwable e) {
			logger.error("Error updating configuration",e);
			throw new ConfigurationUpdateException("An internal error occurred trying to update the configuration:"+e.getMessage());
		}
	}
	
	private void validate(boolean allowRegister, boolean allowEmail, String smtpServer, String fromEmail,boolean smtpAuthRequired,String username,String password, String confirmPassword,String baseuri,String workermemory,String tavernahome) throws ConfigurationUpdateException {
		if (allowEmail) {
			if  (smtpServer.length()<=0) throw new ConfigurationUpdateException("You must provide an SMTP server name");
			if (fromEmail.length()<=0 || !fromEmail.contains("@")) throw new ConfigurationUpdateException("You must provide a valid senders email address");
		}
		
		if (smtpAuthRequired) {
			if (username.length()<=0) throw new ConfigurationUpdateException("You must provide a username if you required SMTP authentication");
			if (!password.equals(confirmPassword)) throw new ConfigurationUpdateException("The password does not match the confirmation password");
		}
		
		try {
			Integer.valueOf(workermemory);
		}
		catch(NumberFormatException e) {
			throw new ConfigurationUpdateException("The memory allocated to a worker must be a number");
		}
		
		try {
			new URL(baseuri);
		}
		catch(MalformedURLException e) {
			throw new ConfigurationUpdateException("The base URI is invalid: "+e.getMessage());
		}
	}

	class ConfigVelocityRepresentation extends VelocityRepresentation {
		
		private Map<String,Object> model = new HashMap<String, Object>();
		
		public ConfigVelocityRepresentation(boolean allowRegister, boolean allowEmail, String smtpServer, String fromEmail, boolean smtpAuthRequired,String username,String password, String confirmPassword,String baseuri,String workermemory,String tavernahome,String errorMsg) {
			model.put("allowregister",allowRegister);
			model.put("allowemail", allowEmail);
			model.put("smtpserver", smtpServer);
			model.put("smtpusername", username);
			model.put("smtppassword",password);
			model.put("smtppasswordconfirm",confirmPassword);
			model.put("smtpauthrequired", smtpAuthRequired);
			model.put("fromemailaddress", fromEmail);
			model.put("workermemory",workermemory);
			model.put("baseuri", baseuri);
			model.put("tavernahome", tavernahome);
			model.put("message",errorMsg);
			model.put("isError",true);
		}
		
		public ConfigVelocityRepresentation(String successMsg) {
			this();
			model.put("message", successMsg);
			model.put("isError", false);
		}
		
		public ConfigVelocityRepresentation() {
			Configuration config = DAOFactory.getFactory().getConfigurationDAO().getConfig();
			model.put("allowregister",config.isAllowRegister());
			model.put("allowemail", config.isAllowEmailNotifications());
			model.put("smtpserver", config.getSmtpServer());
			model.put("smtpusername", config.getSmtpUser());
			model.put("smtppassword",config.getSmtpPassword());
			model.put("smtppasswordconfirm",config.getSmtpPassword());
			model.put("smtpauthrequired", config.isSmtpAuthRequired());
			model.put("fromemailaddress", config.getFromEmail());
			model.put("baseuri", config.getBaseuri());
			model.put("workermemory", config.getWorkerMemory());
			model.put("tavernahome", config.getTavernaHome());
		}

		
		@Override
		protected String pageTitle() {
			return "System configuration";
		}

		@Override
		protected String templateName() {
			return "adminconfig.vm";
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
