package net.sf.taverna.feta.browser.util;

import java.net.URI;
import java.util.Set;

import org.openrdf.elmo.Entity;
import org.restlet.data.Reference;

import uk.org.mygrid.mygridmobyservice.Organisation;
import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class URLFactory {
	private static URLFactory instance;

	public synchronized static URLFactory getInstance() {
		if (instance == null) {
			instance = new URLFactory();
		}
		return instance;
	}

	private Reference root;

	protected URLFactory() {
	}

	public void setRoot(Reference root) {
		this.root = root.getTargetRef();
	}

	public Reference getRoot() {
		return new Reference(root);
	}

	public Reference getReference(Object resource) {
		if (! (resource instanceof Entity)) {
			throw new IllegalArgumentException("Unknown object type " + resource);			
		}
		Entity entity = ((Entity) resource);
		if (resource instanceof ServiceDescription) {
			return new Reference(root, "/services/"
					+ entity.getQName().getLocalPart());
		}
		if (resource instanceof Organisation) {
			Organisation org = (Organisation) resource;
			String name = "unknown";
			Set<String> names = org.getHasOrganisationNameTexts();
			if (! names.isEmpty()) {
				name = names.iterator().next();
			}
			return new Reference(root, "/organisations/" + Reference.encode(name));			
		}
		throw new IllegalArgumentException("Unknown object type " + resource);
	}

}
