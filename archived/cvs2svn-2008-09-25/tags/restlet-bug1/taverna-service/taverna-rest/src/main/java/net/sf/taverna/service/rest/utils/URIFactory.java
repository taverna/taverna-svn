package net.sf.taverna.service.rest.utils;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.OwnedResource;
import net.sf.taverna.service.datastore.bean.UUIDResource;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.rest.RestApplication;

import org.jdom.Attribute;
import org.jdom.Namespace;

public class URIFactory {

	public static final Namespace NS_XLINK =
		Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

	private static URIFactory instance;

	public static synchronized URIFactory getInstance() {
		if (instance == null) {
			instance = new URIFactory();
		}
		return instance;
	}

	private String root = "";

	Map<Class<? extends UUIDResource>, String> resourceMap =
		new HashMap<Class<? extends UUIDResource>, String>();

	private URIFactory() {
		resourceMap.put(Job.class, "/jobs");
		resourceMap.put(Workflow.class, "/workflows");
		resourceMap.put(DataDoc.class, "/data");
		resourceMap.put(User.class, "/users");
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getRoot() {
		return root;
	}

	public String getMapping(Class<? extends UUIDResource> resourceClass) {
		return resourceMap.get(resourceClass);
	}

	/**
	 * Get URI for a resource class.
	 * 
	 * @param resourceClass
	 * @return
	 */
	public String getURI(Class<? extends UUIDResource> resourceClass) {
		if (!resourceMap.containsKey(resourceClass)) {
			throw new IllegalArgumentException("Unknown resource class: "
				+ resourceClass);
		}
		return getRoot() + getMapping(resourceClass);
	}

	public String getURI(UUIDResource resource) {
		String resourcePrefix = getURI(resource.getClass());
		if (resource instanceof User) {
			User user = (User) resource;
			return resourcePrefix + "/" + user.getUsername();
		}
		return resourcePrefix + "/" + resource.getId();
	}

	/**
	 * Get the URI for a collection of owned resources
	 * 
	 * @param user
	 *            The owner of the collection
	 * @param ownedClass
	 *            The class of the collection
	 * @return An URI such as http://blah/user/tom/workflows
	 */
	public String getURI(User owner, Class<? extends OwnedResource> ownedClass) {
		String uri = getURI(owner);
		return uri + getMapping(ownedClass);
	}

	public Attribute getXLink(UUIDResource resource) {
		Attribute xlink = new Attribute("href", getURI(resource), NS_XLINK);
		return xlink;
	}

	public Attribute getXLink(User user,
		Class<? extends OwnedResource> ownedClass) {
		Attribute xlink =
			new Attribute("href", getURI(user, ownedClass), NS_XLINK);
		return xlink;
	}

	public Attribute getXLink(Class<? extends UUIDResource> resourceClass) {
		Attribute xlink =
			new Attribute("href", getURI(resourceClass), NS_XLINK);
		return xlink;
	}

	public String getURIStatus(Job job) {
		return getURI(job) + "/status";
	}

	public String getURIReport(Job job) {
		return getURI(job) + "/report";
	}

	public String getURICurrentUser() {
		return User.class + RestApplication.CURRENT;
	}

}
