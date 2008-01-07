package net.sf.taverna.feta.browser.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sf.taverna.feta.browser.elmo.ServiceRegistry;

public class Utils {

	private static Utils instance;

	private ServiceRegistry serviceRegistry = ServiceRegistry.getInstance();

	public static Utils getInstance() {
		if (instance == null) {
			instance = new Utils();
		}
		return instance;
	}

	protected Utils() {
	}

	public <T> T firstOf(Set<T> set) {
		if (set.isEmpty()) {
			return null;
		}
		return set.iterator().next();
	}

	public List<String> extractBioNames(
			List<org.openrdf.concepts.rdfs.Class> entities) {
		List<String> names = new ArrayList<String>();
		for (org.openrdf.concepts.rdfs.Class entity : entities) {
			if (entity.equals(serviceRegistry.getBioConceptClass())) {
				continue; // Not interesting
			}
			if (entity.getRdfsSubClassOf().contains(
					serviceRegistry.getBioConceptClass())
					&& entity.getQName().getNamespaceURI().equals(
							"http://www.mygrid.org.uk/ontology#")) {
				names.add(entity.getQName().getLocalPart());
			}
		}
		Collections.sort(names);
		return names;
	}
}
