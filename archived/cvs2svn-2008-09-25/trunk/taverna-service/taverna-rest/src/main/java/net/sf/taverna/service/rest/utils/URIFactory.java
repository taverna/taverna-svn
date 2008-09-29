package net.sf.taverna.service.rest.utils;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.AbstractOwned;
import net.sf.taverna.service.datastore.bean.AbstractUUID;
import net.sf.taverna.service.datastore.bean.Configuration;
import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.xml.Capabilities;

import org.apache.log4j.Logger;
import org.restlet.data.Reference;
import org.restlet.data.Request;

/**
 * Generate URIs for resources.
 * <p>
 * The factory needs to be created using {@link #getInstance(Request)} or
 * {@link #getInstance(String)} to specify the application root for generated
 * URIs.
 * <p>
 * To resolve DAO objects from an URI, see {@link URItoDAO}.
 * 
 * @author Stian Soiland
 */
public class URIFactory {

	private static Logger logger = Logger.getLogger(URIFactory.class);

	public static final String HTML = "html";

	public static final String V1 = "v1";

	public static final String DEFAULT_HTML_PATH = "../" + HTML;

	public static boolean BASE_URI_CHANGED = true;

	private static Reference applicationRoot;

	public static URIFactory getInstance() {
		return new URIFactory();
	}

	private static String html = DEFAULT_HTML_PATH;

	private String htmlRoot;

	/**
	 * Mapping from resource collection to path
	 * 
	 */
	static Map<Class<?>, String> resourceMap = new HashMap<Class<?>, String>();

	static {
		resourceMap.put(Job.class, "jobs");
		resourceMap.put(Workflow.class, "workflows");
		resourceMap.put(DataDoc.class, "data");
		resourceMap.put(User.class, "users");
		resourceMap.put(Queue.class, "queues");
		resourceMap.put(Worker.class, "workers");
		resourceMap.put(Configuration.class, "config");
	}

	/**
	 * Get the mapping for a collection resource class. The mapping is generally
	 * appended to {@link #getApplicationRoot()} by {@link #getURI(Class)} or
	 * {@link #getURI(AbstractUUID)}, but also used by
	 * {@link #getURI(User, Class)}.
	 * <p>
	 * If a direct mapping in {@link #resourceMap} can't be found, the mapping
	 * of the superclass will be searched instead, recursively.
	 * 
	 * @see #getURI(Class)
	 * @param resourceClass
	 *            The class of the collection resource
	 * @return The mapping path to add to {@link #getApplicationRoot()}
	 */
	public static String getMapping(Class<?> resourceClass) {
		String mapping = resourceMap.get(resourceClass);
		if (mapping == null && resourceClass.getSuperclass() != null) {
			return getMapping(resourceClass.getSuperclass());
		}
		return mapping;
	}

	/**
	 * Get URI for a resource collection. For owned collections (subclasses of
	 * {@link AbstractOwned}, see {@link #getURI(User, Class)}
	 * 
	 * @param resourceClass
	 *            Class of the resource, for example {@link User}
	 * @return The full URI to the collection of resources of the given class
	 */
	public String getURI(Class<? extends AbstractUUID> resourceClass) {
		String mapping = getMapping(resourceClass);
		if (mapping == null) {
			throw new IllegalArgumentException("Unknown resource class: "
					+ resourceClass);
		}
		return new Reference(getApplicationRoot(), mapping).getTargetRef()
				.toString();
	}

	/**
	 * Get URI for a resource.
	 * 
	 * @param resource
	 *            The {@link AbstractUUID} bean of the resource
	 * @return The full URI for the resource
	 */
	public String getURI(AbstractUUID resource) {
		if (resource.getClass().equals(User.class)) {
			// But not Worker as that is resolved by ID
			return getURIUser((User) resource);
		}
		String resourcePrefix = getURI(resource.getClass());
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
		String uri = getURIUser(owner);
		return uri + "/" + getMapping(ownedClass);
	}

	public String getURIUser(User user) {
		return getURI(User.class) + "/" + user.getUsername();
	}

	public String getURIEditUser(User user) {
		return getURIUser(user) + getMappingEditUser();
	}

	public String getURIDefaultQueue() {
		return getURI(Queue.class) + getMappingDefaultQueue();
	}

	/**
	 * Get the URI for the status of a job
	 * 
	 * @param job
	 *            The {@link Job} bean
	 * @return The full URI for the job's status
	 */
	public String getURIStatus(Job job) {
		return getURI(job) + getMappingStatus();
	}

	/**
	 * Get the URI for the progress report of a job
	 * 
	 * @param job
	 *            The {@link Job} bean
	 * @return The full URI for the job's progress report
	 */
	public String getURIReport(Job job) {
		return getURI(job) + getMappingReport();
	}

	/**
	 * Get the URI for the console log of a job
	 * 
	 * @param job
	 *            The {@link Job} bean
	 * @return The full URI for the job's console log
	 */
	public String getURIConsole(Job job) {
		return getURI(job) + getMappingConsole();
	}

	/**
	 * The URI for getting the currently authenticated user. This URI will
	 * normally redirect to the actual URI of the current user, and is normally
	 * included in the {@link Capabilities} document.
	 * 
	 * @see Capabilities#getCurrentUser()
	 * @return The full URI for the meta-resource that redirects to the current
	 *         user
	 */
	public String getURICurrentUser() {
		return getURI(User.class) + getMappingCurrentUser();
	}

	public String getURICreateAdmin() {
		return getURI(User.class) + getMappingCreateAdmin();
	}

	/**
	 * The mapping added to the URI of the {@link User} collection to form the
	 * URL for the current user.
	 * 
	 * @see #getURI(Class)
	 * @see #getURICurrentUser()
	 * @return The suffix to be attached to the User collection URI.
	 */
	public static String getMappingCurrentUser() {
		return ";current";
	}

	/**
	 * The mapping to the URI of the {@link Queue} to the form the URL for the
	 * default Queue.
	 * 
	 * @return
	 */
	public static String getMappingDefaultQueue() {
		return ";default";
	}

	/**
	 * The mapping added to the URI of the {@link Job} to get the status.
	 * 
	 * @see #getURIStatus(Job)
	 * @see #getURI(AbstractUUID)
	 * @return The suffix to be attached to the Job resource URI
	 */
	public static String getMappingStatus() {
		return "/status";
	}

	public static String getMappingRegisterUser() {
		return ";register";
	}

	public static String getMappingCreateAdmin() {
		return ";createAdmin";
	}

	public static String getMappingAddUser() {
		return ";add";
	}

	public static String getMappingEditUser() {
		return "/edit";
	}

	/**
	 * The mapping added to the URI of the {@link Job} to get the console.
	 * 
	 * @see #getURIConsole(Job))
	 * @see #getURI(AbstractUUID)
	 * @return The suffix to be attached to the Job resource URI
	 */
	public static String getMappingConsole() {
		return "/console";
	}

	/**
	 * The mapping added to the URI of the {@link Job} to get the progress
	 * report.
	 * 
	 * @see #getURIReport(Job)
	 * @see #getURI(AbstractUUID)
	 * @return The suffix to be attached to the Job resource URI
	 */
	public static String getMappingReport() {
		return "/report";
	}

	/**
	 * Set the HTML path to add to the root URI for static HTML files, relative
	 * to the application root, typically "../html". The default is
	 * <code>{@value #DEFAULT_HTML}</code>
	 * <p>
	 * The path can also be absolute if it is not expressible relative to the
	 * application root.
	 * 
	 * @param path
	 */
	public static void setHTMLpath(String path) {
		html = path;
	}

	/**
	 * Get the path relative to {@link #getApplicationRoot()} for static HTML
	 * files.
	 * 
	 * @return
	 */
	public static String getHTMLpath() {
		return html;
	}

	/**
	 * Get the application root. The root is where the {@link Capabilities}
	 * document is served, and also the base of the URIs to collections such as
	 * <code>users</code> defined by {@link #resourceMap}
	 * 
	 * @return The application root as a {@link Reference}
	 */
	public Reference getApplicationRoot() {
		if (applicationRoot == null || BASE_URI_CHANGED) {
			DAOFactory daoFactory = DAOFactory.getFactory();
			Configuration config = daoFactory.getConfigurationDAO().getConfig();
			String root = config.getBaseuri();
			if (daoFactory.hasActiveTransaction())
				daoFactory.rollback();
			daoFactory.close();
			if (!root.endsWith("/"))
				root += "/";
			applicationRoot = new Reference(root + URIFactory.V1 + "/");
			logger
					.info("Set application root to "
							+ applicationRoot.toString());
			BASE_URI_CHANGED = false;
		}
		return applicationRoot;
	}

	/**
	 * Get the calculated root of static HTML files. This is normally calculated
	 * from the relative address of {@link #getHTMLpath()} from
	 * {@link #getApplicationRoot()}. If the {@link #getHTMLpath()} address is
	 * absolute, that address will be used instead.
	 * 
	 * @return The root URI of static HTML files
	 */
	public String getHTMLRoot() {
		if (htmlRoot == null) {
			htmlRoot = new Reference(getApplicationRoot(), getHTMLpath())
					.getTargetRef().toString();
		}
		return htmlRoot;
	}

	@Override
	public String toString() {
		return super.toString() + ": " + getApplicationRoot();
	}

	public String getURIRegisterAdmin() {
		return null;
	}

	public String getURIAddUser() {
		return getURI(User.class) + getMappingAddUser();
	}

	public String getURIRegister() {
		return getURI(User.class) + getMappingRegisterUser();
	}

	public String getURIEntityList(DataDoc dataDoc, String portName, String lsid) {
		return getURI(dataDoc) + getMappingPorts() + "/" + portName + 
			getMappingEntityList() + "/" + lsid;
	}

	public static String getMappingPorts() {
		return "/ports";
	}
	
	public static String getMappingEntityList() {
		return "/list";
	}
	
	public static String getMappingStringLiteral() {
		return "/entity";
	}

	public String getURIStringLiteral(DataDoc dataDoc, String portName,
			String lsid) {
		return getURI(dataDoc) + getMappingPorts() + "/" + portName + 
		getMappingStringLiteral() + "/" + lsid;
	}

}
