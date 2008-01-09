package net.sf.taverna.feta.browser.util;

import java.net.URI;

import org.apache.log4j.Logger;
import org.openrdf.elmo.Entity;
import org.restlet.data.Reference;

import uk.org.mygrid.mygridmobyservice.Organisation;
import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class URIFactory {

	private static URIFactory instance;

	private static Logger logger = Logger.getLogger(URIFactory.class);

	private static Utils setUtils = Utils.getInstance();

	public synchronized static URIFactory getInstance() {
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
			return getURIForServices()
					.resolve(entity.getQName().getLocalPart());
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
