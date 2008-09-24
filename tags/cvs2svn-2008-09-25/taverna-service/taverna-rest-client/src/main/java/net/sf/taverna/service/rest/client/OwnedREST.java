package net.sf.taverna.service.rest.client;

import static net.sf.taverna.service.rest.client.RESTContext.xmlOptions;

import java.util.Calendar;

import net.sf.taverna.service.xml.JobDocument;
import net.sf.taverna.service.xml.Owned;
import net.sf.taverna.service.xml.User;

import org.restlet.data.Reference;

public class OwnedREST<OwnedClass extends Owned> extends LinkedREST<OwnedClass> {

	public OwnedREST(RESTContext context, OwnedClass document) {
		super(context, document);
	}

	public OwnedREST(RESTContext context, Reference uri, OwnedClass document) {
		super(context, uri, document);
	}

	public OwnedREST(RESTContext context, Reference uri, Class<OwnedClass> documentClass) {
		super(context, uri, documentClass);
	}
		
	/**
	 * Return the owner, or <code>null</code> if the resource does not have an
	 * owner.
	 */
	public UserREST getOwner() {
		User owner = getDocument().getOwner();
		if (owner == null) {
			return null;
		}
		return new UserREST(context, owner);
	}

	
	public void setTitle(String title) throws NotSuccessException {
		JobDocument job = JobDocument.Factory.newInstance(xmlOptions);
		job.addNewJob().setTitle(title);
		context.put(getURIReference(), job);
		invalidate();
	}
	
	public String getTitle() {
		String title = getCurrentDocument().getTitle();
		if (title != null) {
			return title;
		}
		return getDocument().getTitle();
	}

	public Calendar getCreated() {
		Calendar created = getCurrentDocument().getCreated();
		if (created != null) {
			return created;
		}
		return getDocument().getCreated();
	}

	public Calendar getLastModified() {
		Calendar modified = getCurrentDocument().getModified();
		if (modified != null) {
			return modified;
		}
		return getDocument().getModified();
	}

}