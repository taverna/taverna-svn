package net.sf.taverna.feta.browser.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import net.sf.taverna.feta.browser.elmo.ServiceRegistry;

import org.openrdf.concepts.rdfs.Class;

public class Utils {

	private static Utils instance;

	public static Utils getInstance() {
		if (instance == null) {
			instance = new Utils();
		}
		return instance;
	}

	private ServiceRegistry serviceRegistry = ServiceRegistry.getInstance();

	protected Utils() {
	}

	public List<String> extractBioNames(
			List<org.openrdf.concepts.rdfs.Class> entities) {
		List<String> names = new ArrayList<String>();
		Collections.sort(entities,
				new Comparator<org.openrdf.concepts.rdfs.Class>() {
					public int compare(Class c1, Class c2) {
						// Poor mans "ontology"-sorting
						if (c1 == c2 || c1.equals(c2)) {
							return 0;
						}
						if (c1.getRdfsSubClassOf().contains(c2)) {
							return 1; // c1 after parent c2
						}
						if (c2.getRdfsSubClassOf().contains(c1)) {
							return -1; // c2 after parent c1;
						}
						return c1.getQName().toString().compareTo(
								c2.getQName().toString());
					}
				});
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
		// Collections.sort(names);
		return names;
	}

	public <T> T firstOf(Set<T> set) {
		if (set.isEmpty()) {
			return null;
		}
		return set.iterator().next();
	}
	
	public String htmlURLs(String plainText) {
		// Naive regex to find URLs
		return plainText.replaceAll("(http://[^ \n]+)", 
				"<a href=\"$1\">$1</a>");
	}

}
