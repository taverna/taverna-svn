package net.sf.taverna.service.rest.resources;

import java.io.IOException;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.util.XMLUtils;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

public class UsersResource extends AbstractResource {
	
	// Should be enough to hold username/password/email
	private static final int MAX_USER = 16384;

	public UsersResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().clear();
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
	}

	private static Logger logger = Logger.getLogger(UsersResource.class);
	
	@Override
	public boolean allowGet() {
		return false;
	}
	
	@Override
	public boolean allowPost() {
		return true;
	}
	
	@Override
	public long maxSize() {
		return MAX_USER;
	}
	
	@Override
	public void post(Representation entity) {
		if (! AbstractResource.restType.includes(entity.getMediaType())) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, 
				"Content type must be " + AbstractResource.restType);
			return;
		}		
		if (overMaxSize(entity)) {
			logger.warn("Uploaded user document was too large: " + entity.getSize());
			return;
		}
		
		Document doc;
		try {
			doc = XMLUtils.parseXML(entity.getStream());
		} catch (ParseException e) {
			logger.warn("Could not parse user document", e);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
				"Could not parse as XML");
			return;
		} catch (IOException e) {
			logger.warn("Could not read user XML", e);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
				"Could not read XML");
			return;
		}
		Element userElement = doc.getRootElement();
		if (! userElement.getName().equals("user") || ! userElement.getNamespace().equals(ns)) {
			logger.warn("Not a user document: " + doc);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
				"Not a user document");
			return;
		}
		Element userNameElement = userElement.getChild("username", ns);
		User user;
		if (userNameElement != null) {
			String userName = userNameElement.getTextNormalize();
			// Check if already exist
			if (daoFactory.getUserDAO().readByUsername(userName) != null) {
				logger.warn("Username already exists: " + userName);
				getResponse().setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED,
					"Username already exists: " + userName);
				return;
			}
			user = new User(userName);
		} else {
			user = new User();
		}
		
		String generatedPassword = null;
		Element passwordElement = userElement.getChild("password", ns);
		if (passwordElement != null) {
			user.setPassword(passwordElement.getTextNormalize());
		} else {
			generatedPassword = User.generatePassword();
			user.setPassword(generatedPassword);
		}
		Element emailElement = userElement.getChild("email", ns);
		if (emailElement != null) {
			user.setEmail(emailElement.getTextNormalize());
		}

		daoFactory.getUserDAO().create(user);
		daoFactory.commit();
		getResponse().setStatus(Status.SUCCESS_CREATED);
		System.out.println("Created " + uriFactory.getURI(user));
		getResponse().setRedirectRef(uriFactory.getURI(user));
		if (generatedPassword != null) {
			System.out.println("Password generated: " + generatedPassword);
			getResponse().setEntity(generatedPassword, MediaType.TEXT_PLAIN);
		}
	}

	@Override
	public String representPlainText() {
		return null;
	}
	
	
}
