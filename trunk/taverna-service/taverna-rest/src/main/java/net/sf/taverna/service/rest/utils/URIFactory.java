package net.sf.taverna.service.rest.utils;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.AbstractOwned;
import net.sf.taverna.service.datastore.bean.AbstractUUID;
import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.bean.Workflow;

import org.apache.log4j.Logger;
import org.jdom.Namespace;
import org.restlet.data.Reference;
import org.restlet.data.Request;

public class URIFactory {
	
	private static Logger logger = Logger.getLogger(URIFactory.class);

	public static final String HTML = "html" ;
	
	public static final String V1 = "v1";
	
	public static final String DEFAULT_HTML_PATH = "../" + HTML;

	public static final Namespace NS_XLINK =
		Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

	public static URIFactory getInstance(String root) {
		return new URIFactory(root);
	}

	public static URIFactory getInstance(Request request) {
		return new URIFactory(request);
	}

	private static String html = DEFAULT_HTML_PATH;

	private Reference applicationRoot;
	
	private Request request;

	private String htmlRoot;

	static Map<Class<?>, String> resourceMap = new HashMap<Class<?>, String>();

	static {
		resourceMap.put(Job.class, "jobs");
		resourceMap.put(Workflow.class, "workflows");
		resourceMap.put(DataDoc.class, "data");
		resourceMap.put(User.class, "users");
		resourceMap.put(Queue.class, "queues");
		resourceMap.put(Worker.class, "workers");
	}

	private URIFactory(String applicationRoot) {
		setApplicationRoot(applicationRoot);
	}

	public URIFactory(Request request) {
		this.request = request;
		setApplicationRoot(request.getRootRef());
	}


	public static String getMapping(Class<?> resourceClass) {
		String mapping = resourceMap.get(resourceClass);
		if (mapping == null && resourceClass.getSuperclass() != null) {
			return getMapping(resourceClass.getSuperclass());
		}
		return mapping;
	}

	/**
	 * Get URI for a resource class.
	 * 
	 * @param resourceClass
	 * @return
	 */
	public String getURI(Class<? extends AbstractUUID> resourceClass) {
		String mapping = getMapping(resourceClass);
		if (mapping == null) {
			throw new IllegalArgumentException("Unknown resource class: "
				+ resourceClass);
		}
		return new Reference(getApplicationRoot(), mapping).getTargetRef().toString();
	}

	public String getURI(AbstractUUID resource) {
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
	public String getURI(User owner, Class<? extends AbstractOwned> ownedClass) {
		String uri = getURI(owner);
		return uri + "/" + getMapping(ownedClass);
	}

	public String getURIStatus(Job job) {
		return getURI(job) + getMappingStatus();
	}

	public String getURIReport(Job job) {
		return getURI(job) + getMappingReport();
	}

	public String getURICurrentUser() {
		return getURI(User.class) + getMappingCurrentUser();
	}

	public static String getMappingCurrentUser() {
		return ";current";
	}

	public static String getMappingStatus() {
		return "/status";
	}

	public static String getMappingReport() {
		return "/report";
	}

	/**
	 * Set the HTML path to add to the root URI for static HTML files, relative
	 * to the application root, typically "../html". The default is
	 * <code>{@value #DEFAULT_HTML}</code>
	 * 
	 * @param path
	 */
	public static void setHTMLpath(String path) {
		html = path;
	}

	public static String getHTMLpath() {
		return html;
	}

	public void setApplicationRoot(Reference root) {
		logger.debug(root + " " + root.getBaseRef());
		applicationRoot = root.getTargetRef();
		if (! applicationRoot.getPath().endsWith("/")) {
			applicationRoot.setPath(applicationRoot.getPath() + "/");
		}
		logger.debug("Set application root to " + applicationRoot);
	}
	
	public void setApplicationRoot(String root) {
		setApplicationRoot(new Reference(root));
	}
	

	public Reference getApplicationRoot() {
		return applicationRoot;
	}

	public String getHTMLRoot() {
		if (htmlRoot == null) {
			htmlRoot =
				new Reference(getApplicationRoot(), getHTMLpath()).getTargetRef().toString();
		} 
		return htmlRoot;
	}

	@Override
	public String toString() {
		return super.toString() + ": " + getApplicationRoot();
	}
	
}
