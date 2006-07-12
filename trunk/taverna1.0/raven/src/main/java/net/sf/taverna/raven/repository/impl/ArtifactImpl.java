/**
 * 
 */
package net.sf.taverna.raven.repository.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.ArtifactStateException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A single Maven2 artifact with group, artifact and version
 * @author Tom
 */
public class ArtifactImpl extends BasicArtifact {
	
	private LocalRepository repository;
	private String packageType = null;
	private List<ArtifactImpl> dependencies = null;
	
	/**
	 * Create a new Artifact description
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param repository
	 */
	ArtifactImpl(String groupId, String artifactId, String version, LocalRepository repository) {
		super(groupId, artifactId, version);
		this.repository = repository;
	}
	
	/**
	 * Create a new ArtifactImpl from an Artifact and a Repository
	 */
	ArtifactImpl(Artifact a, LocalRepository repository) {
		super(a.getGroupId(), a.getArtifactId(), a.getVersion());
		this.repository = repository;
	}
	
	@Override
	public String toString() {
		return getGroupId()+":"+getArtifactId()+":"+getVersion();
	}
	
	public String getPackageType() {
		if (packageType != null) {
			return this.packageType;
		}
		File pomFile = this.repository.pomFile(this);
		if (pomFile.exists()) {
			InputStream is;
			try {
				is = pomFile.toURL().openStream();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(is);
				List<Node> l = findElements(document, "packaging");
				if (l.isEmpty()) {
					this.packageType = "jar";
					return this.packageType;
				}
				Node packageNode = l.iterator().next();
				this.packageType = packageNode.getFirstChild().getNodeValue().trim();
				return this.packageType;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Analyse the corresponding .pom and return a list of all
	 * immediate dependencies from this artifact. If no repository
	 * is defined then return an empty list.
	 * @return List of Artifacts upon which this depends
	 */
	public synchronized List<ArtifactImpl> getDependencies() throws ArtifactStateException {
		if (this.dependencies != null) {
			return dependencies;
		}
		List<ArtifactImpl> result = new ArrayList<ArtifactImpl>();
		if (repository == null) {
			// Should never get here, it's impossible to construct an ArtifactImpl with
			// a null repository.
			return result;
		}
		ArtifactStatus status = repository.getStatus(this);
		if (status.getOrder() < ArtifactStatus.Pom.getOrder() || status.isError()) {
			throw new ArtifactStateException(status, new ArtifactStatus[]{ArtifactStatus.Analyzed,
					ArtifactStatus.Jar, ArtifactStatus.Pom, ArtifactStatus.Ready});
		}
		
		File pomFile = this.repository.pomFile(this);
		if (pomFile.exists()) {
			InputStream is;
			try {
				is = pomFile.toURL().openStream();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(is);
				List<Node> elementList = findElements(document, "dependency");
				for (Node node : elementList) {
					node.normalize();
					Node n = findElements(node, "groupId" ).iterator().next();
					String groupId = n.getFirstChild().getNodeValue().trim();
					n = findElements(node, "artifactId" ).iterator().next();
					String artifactId = n.getFirstChild().getNodeValue().trim();
					n = findElements(node, "version" ).iterator().next();
					String version = n.getFirstChild().getNodeValue().trim();
					result.add(new ArtifactImpl(groupId, artifactId, version, this.repository));
				}				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			// TODO Handle absence of pom file here
		}
		this.dependencies = result;
		return result;
	}
	
	private List<Node> findElements(Node fromnode, String name) {
		NodeList nodelist = fromnode.getChildNodes();
		List<Node> list = new ArrayList<Node>();
		for (int i=0; i<nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (name.equals(node.getNodeName())) {
					list.add(node);
				}
				list.addAll(findElements(node, name));
			}    		
		}
		return list;
	}

	
}
