package net.sf.taverna.service.rest.resources;

import static net.sf.taverna.service.rest.utils.XMLBeansUtils.xmlOptions;

import java.io.IOException;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.rest.UserGuard;
import net.sf.taverna.service.xml.User;
import net.sf.taverna.service.xml.UserDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

public class UserResource extends AbstractUserResource {

	private static Logger logger = Logger.getLogger(UserResource.class);

	// Should be enough to hold username/password/email
	public static final int MAX_USER = 16384;

	public UserResource(Context context, Request request, Response response) {
		super(context, request, response);
		addRepresentation(new Text());
		addRepresentation(new XML());
	}

	class XML extends AbstractREST {
		@Override
		public XmlObject getXML() {
			UserDocument userDoc = UserDocument.Factory.newInstance(xmlOptions);
			User userElem = userDoc.addNewUser();
			userElem.setUsername(user.getUsername());
			if (user.getEmail() != null) {
				userElem.setEmail(user.getEmail());
			}
			userElem.addNewWorkflows().setHref(
				uriFactory.getURI(user, Workflow.class));
			userElem.addNewJobs().setHref(uriFactory.getURI(user, Job.class));
			userElem.addNewDatas().setHref(uriFactory.getURI(user, DataDoc.class));
			return userDoc;
		}
	}

	class Text extends AbstractText {
		@Override
		public String getText() {
			StringBuilder sb = new StringBuilder();
			sb.append("User ").append(user.getUsername()).append('\n');
			sb.append("Created: ").append(user.getCreated()).append('\n');
			sb.append("Last-Modified: ").append(user.getLastModified()).append(
				'\n');
			if (user.getEmail() != null) {
				sb.append("Email: ").append(user.getEmail());
			}
			if (!user.getJobs().isEmpty()) {
				sb.append("Jobs:");
				for (Job j : user.getJobs()) {
					sb.append(" ");
					sb.append(uriFactory.getURI(j));
					sb.append("\n");
				}
			}
			if (!user.getWorkflows().isEmpty()) {
				sb.append("Workflows:");
				for (Workflow wf : user.getWorkflows()) {
					sb.append(" ");
					sb.append(uriFactory.getURI(wf));
					sb.append("\n");
				}
			}
			return sb.toString();
		}
	}

	@Override
	public long maxSize() {
		return MAX_USER;
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void post(Representation entity) {
		if (overMaxSize(entity)) {
			return;
		}
		// TODO: Support text/plain POST as well
		if (!restType.includes(entity.getMediaType())) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
				"Content type should be " + AbstractResource.restType);
			return;
		}

		// Double-check that the user is changing his own data
		// (Should already have been checked by checkEntity(user), but
		// we'll double-check because we might be dealing with passwords here)
		if (!user.equals(getContext().getAttributes().get(
			UserGuard.AUTHENTICATED_USER))) {
			challenge();
			return;
		}

		User userElem;
		try {
			userElem =
				UserDocument.Factory.parse(entity.getStream(), xmlOptions).getUser();
		} catch (IOException e) {
			logger.warn("Could not read user XML", e);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
				"Could not read XML");
			return;
		} catch (XmlException e) {
			logger.warn("Could not parse user document", e);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
				"Could not parse as XML");
			return;
		}

		if (userElem.getEmail() != null) {
			user.setEmail(userElem.getEmail());
			logger.info("Set email address for " + user);
		}
		if (userElem.getPassword() != null) {
			user.setPassword(userElem.getPassword());
			logger.info("Set password for " + user);
		}
		daoFactory.commit();
		getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
	}

}
