package net.sf.taverna.service.rest.resources;

import java.io.IOException;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.xml.UserDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

public class UsersResource extends AbstractResource {

	private static Logger logger = Logger.getLogger(UsersResource.class);

	public UsersResource(Context context, Request request, Response response) {
		super(context, request, response);
	}

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
		return UserResource.MAX_USER;
	}

	@Override
	public void post(Representation entity) {
		if (!restType.includes(entity.getMediaType())) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
				"Content type must be " + AbstractResource.restType);
			return;
		}
		if (overMaxSize(entity)) {
			logger.warn("Uploaded user document was too large: "
				+ entity.getSize());
			return;
		}
		UserDocument doc;
		try {
			doc = UserDocument.Factory.parse(entity.getStream());
		} catch (XmlException e) {
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
		net.sf.taverna.service.xml.User userElem = doc.getUser();
		String userName = userElem.getUsername();			
		User user;
		if (userName != null) {
			userName = userName.trim();
			// Check if already exist
			if (daoFactory.getUserDAO().readByUsername(userName) != null) {
				logger.warn("Username already exists: " + userName);
				getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,
					"Username already exists: " + userName);
				return;
			}
			user = new User(userName);
		} else {
			user = new User();
		}

		String generatedPassword = null;
		String password = userElem.getPassword();
		if (password != null) {
			user.setPassword(password);
		} else {
			generatedPassword = User.generatePassword();
			user.setPassword(generatedPassword);
		}
		if (userElem.getEmail() != null) {
			user.setEmail(userElem.getEmail());
		}
		
		daoFactory.getUserDAO().create(user);
		daoFactory.commit();
		logger.info("Created " + user);
		getResponse().setStatus(Status.SUCCESS_CREATED);
		getResponse().setRedirectRef(uriFactory.getURI(user));
		if (generatedPassword != null) {
			logger.debug("Password: " + generatedPassword);
			getResponse().setEntity(generatedPassword, MediaType.TEXT_PLAIN);
		}
	}

}
