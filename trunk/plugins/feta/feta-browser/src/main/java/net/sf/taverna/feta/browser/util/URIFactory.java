package net.sf.taverna.feta.browser.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sf.taverna.feta.browser.elmo.ServiceRegistry;

import org.apache.log4j.Logger;
import org.openrdf.elmo.Entity;
import org.restlet.data.Reference;
import org.restlet.data.Request;

import uk.org.mygrid.mygridmobyservice.Organisation;
import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class URIFactory {

	private static Utils setUtils = Utils.getInstance();
	
	private static URIFactory instance;

	private static Logger logger = Logger.getLogger(URIFactory.class);

	private static ServiceRegistry serviceRegistry = ServiceRegistry.getInstance();

	public static URIFactory getInstance(Request request) {
		Reference root = request.getRootRef();
		URI rootUri = URI.create(root.getTargetRef().toString() + "/")
				.normalize();
		URIFactory uriFactory = new URIFactory();
		uriFactory.setRoot(rootUri);
		return uriFactory;
	}

	private synchronized static URIFactory getInstance() {
		if (instance == null) {
			instance = new URIFactory();
		}
		return instance;
	}

	private URI root = URI.create("/");

	protected URIFactory() {
	}

	public URI getRoot() {
		return root;
	}



	public URI getURI(Object resource) {
		if (!(resource instanceof Entity)) {
			throw new IllegalArgumentException("Unknown object type "
					+ resource);
		}
		Entity entity = ((Entity) resource);
		if (resource instanceof ServiceDescription) {
			return getURIForService((ServiceDescription) resource);
		}
		if (resource instanceof Organisation) {
			Organisation org = (Organisation) resource;
			String name = setUtils.firstOf(org.getHasOrganisationNameTexts());
			if (name == null) {
				name = "unknown";
			}
			return getURIForOrganisation(name);
		}
		throw new IllegalArgumentException("Unknown object type " + resource);
	}

	public URI getURIForMethod(String name) {
		return getURIForMethods().resolve(Reference.encode(name));
	}

	public URI getURIForMethods() {
		return root.resolve("methods/");
	}

	public URI getURIForNamespace(String namespace) {
		return getURIForNamespaces().resolve(namespace);
	}

	public URI getURIForNamespaces() {
		return root.resolve("namespaces/");
	}

	public URI getURIForOrganisation(String name) {
		return getURIForOrganisations().resolve(Reference.encode(name));
	}

	public URI getURIForOrganisations() {
		return root.resolve("organisations/");
	}

	public URI getURIForResource(String name) {
		return getURIForResources().resolve(Reference.encode(name));
	}

	public URI getURIForResources() {
		return root.resolve("resources/");
	}

	public URI getURIForService(ServiceDescription service) {
		String hashHex = serviceRegistry.getServiceHash(service);
		return getURIForServices().resolve(hashHex);
	}

	public URI getURIForServices() {
		return root.resolve("services/");
	}

	public URI getURIForTask(String name) {
		return getURIForTasks().resolve(name);
	}

	public URI getURIForTasks() {
		return root.resolve("tasks/");
	}

	public URI getURIForType(String type) {
		return getURIForTypes().resolve(type);
	}

	public URI getURIForTypes() {
		return root.resolve("types/");
	}

	public URI getURIForUpdate() {
		return root.resolve("registry;update");
	}

	public void setRoot(URI root) {
		this.root = root;
	}

}
