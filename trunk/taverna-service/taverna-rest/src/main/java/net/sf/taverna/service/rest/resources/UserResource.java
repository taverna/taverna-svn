package net.sf.taverna.service.rest.resources;

import net.sf.taverna.service.xml.User;
import net.sf.taverna.service.xml.UserDocument;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Workflow;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class UserResource extends AbstractUserResource {

	public UserResource(Context context, Request request, Response response) {
		super(context, request, response);
	}

	@Override
	public String representXML() {
		UserDocument userDoc = UserDocument.Factory.newInstance(xmlOptions);
		User userElem = userDoc.addNewUser();
		userElem.setId(user.getId());
		userElem.setUsername(user.getUsername());
		userElem.addNewWorkflows().setHref(
			uriFactory.getURI(user, Workflow.class));
		userElem.addNewJobs().setHref(uriFactory.getURI(user, Job.class));
		String a = userDoc.xmlText(xmlOptions);
		System.out.println("Returning:\n" + a);
		return a;
	}

	@Override
	public String representPlainText() {
		StringBuilder sb = new StringBuilder();
		sb.append("User ").append(user.getUsername()).append('\n');
		sb.append("Created: ").append(user.getCreated()).append('\n');
		sb.append("Last-Modified: ").append(user.getLastModified()).append('\n');

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
