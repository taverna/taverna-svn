package org.embl.ebi.escience.scuflworkers.dependency;

import static org.embl.ebi.escience.scufl.XScufl.XScuflNS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.raven.repository.BasicArtifact;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflworkers.dependency.DependencyProcessor.ClassLoaderSharing;
import org.jdom.Attribute;
import org.jdom.Element;

public class DependencyXMLHandler {

	private static Logger logger = Logger.getLogger(DependencyXMLHandler.class);
	
	public static List<Element> saveDependencies(DependencyProcessor bp) {
		List<Element> elements = new ArrayList<Element>();
		
		Element dependencies = new Element("dependencies", XScuflNS);
		dependencies.setAttribute("classloader",
			bp.getClassLoaderSharing().name(), XScuflNS);
		for (String jarFile : bp.localDependencies) {
			Element jarElement = new Element("jarfile", XScuflNS);
			jarElement.setText(jarFile);
			dependencies.addContent(jarElement);
		}
		for (BasicArtifact artifact : bp.artifactDependencies) {
			Element artifactElement = new Element("artifact", XScuflNS);
			artifactElement.setAttribute("groupId", artifact.getGroupId(), XScuflNS);
			artifactElement.setAttribute("artifact", artifact.getArtifactId(), XScuflNS);
			artifactElement.setAttribute("version", artifact.getVersion(), XScuflNS);
			dependencies.addContent(artifactElement);			
		}
		// Always include, even without any dependencies, as we need to store
		// our classloader preference
		elements.add(dependencies);
		
		Element repositories = new Element("repositories", XScuflNS);
		for (URL url : bp.repositories) {
			Element repository = new Element("repository", XScuflNS);
			repository.setText(url.toExternalForm());
			repositories.addContent(repository);
		}
		if (repositories.getContentSize() > 0) {
			elements.add(repositories);
		}
		return elements;
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
			List<Element> artifacts = dependencies.getChildren("artifact", XScuflNS);
			for (Element artifact : artifacts) {
				String groupId = artifact.getAttributeValue("groupId", XScuflNS);
				String artifactId = artifact.getAttributeValue("artifactId", XScuflNS);
				String version = artifact.getAttributeValue("version", XScuflNS);
				try {
					BasicArtifact a = new BasicArtifact(groupId, artifactId, version);
					bp.artifactDependencies.add(a);			
				} catch (NullPointerException e) {
					logger.warn("Missing one of groupId, artifactId or version for: " + artifact);
				}
			}
		}

		Element repositoryElement = processorElement.getChild("repositories", XScuflNS);
		if (repositoryElement == null) {
			return;
		}
		List<Element> repositories = repositoryElement.getChildren("repository", XScuflNS);
		for (Element repository : repositories) {
			try {
				URL url = new URL(repository.getTextTrim());
				bp.repositories.add(url);
			} catch (MalformedURLException e) {
				logger.warn("Invalid repository URL: " + repository.getTextTrim(), e);
			}
		}
		
	}
}

