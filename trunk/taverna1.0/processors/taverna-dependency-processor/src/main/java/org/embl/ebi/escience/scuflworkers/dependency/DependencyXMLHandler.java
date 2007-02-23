package org.embl.ebi.escience.scuflworkers.dependency;

import static org.embl.ebi.escience.scufl.XScufl.XScuflNS;

import java.util.List;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflworkers.dependency.DependencyProcessor.ClassLoaderSharing;
import org.jdom.Attribute;
import org.jdom.Element;

public class DependencyXMLHandler {

	private static Logger logger = Logger.getLogger(DependencyXMLHandler.class);
	
	public static Element saveDependencies(DependencyProcessor bp) {
		Element dependencies = new Element("dependencies", XScuflNS);
		dependencies.setAttribute("classloader",
			bp.getClassLoaderSharing().name(), XScuflNS);
		for (String jarFile : bp.localDependencies) {
			Element jarElement = new Element("jarfile", XScuflNS);
			dependencies.addContent(jarElement);
			jarElement.setText(jarFile);
		}
		return dependencies;
	}

	@SuppressWarnings("unchecked")
	public static void loadDependencies(DependencyProcessor bp, Element processorElement) {
		Element dependencies = processorElement.getChild("dependencies", XScuflNS);
		if (dependencies != null) {
			List<Element> jarFiles = dependencies.getChildren("jarfile", XScuflNS);
			for (Element jarFile : jarFiles) {
				bp.localDependencies.add(jarFile.getTextTrim());
			}	
			Attribute attr = dependencies.getAttribute("classloader", XScuflNS);
			if (attr != null) {
				try {
					ClassLoaderSharing sharing = ClassLoaderSharing.valueOf(attr.getValue());
					bp.setClassLoaderSharing(sharing);
				} catch (IllegalArgumentException ex) {
					logger.error("Unknown classloader sharing: " + attr.getValue());
				}
			}
		}
	}
}
