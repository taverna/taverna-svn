/**
 * 
 */
package net.sf.taverna.raven.repository.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
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
	
	private ArtifactImpl parentArtifact = null;
	/**
	 * Force all parent pom XML files to exist within the repository, set up the
	 * parentArtifact field if one is found
	 */
	private synchronized void checkParent(File pomFile) {
		InputStream is;
		try {
			is = pomFile.toURL().openStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(is);
			is.close();
			List<Node> elementList = findElements(document, "parent");
			if (elementList.isEmpty() == false) {
				Node parentNode = elementList.iterator().next();
				Node n = findElements(parentNode, "groupId" ).iterator().next();
				String parentGroupId = n.getFirstChild().getNodeValue().trim();
				n = findElements(parentNode, "artifactId" ).iterator().next();
				String parentArtifactId = n.getFirstChild().getNodeValue().trim();
				n = findElements(parentNode, "version" ).iterator().next();
				String parentVersion = n.getFirstChild().getNodeValue().trim();
				parentArtifact = new ArtifactImpl(parentGroupId,parentArtifactId,parentVersion,this.repository);
				this.repository.addArtifact(parentArtifact);
				if (this.repository.getStatus(parentArtifact).equals(ArtifactStatus.Queued)) {
					// Force a fetch of the pom file
					this.repository.forcePom(parentArtifact);
				}
				parentArtifact.checkParent(this.repository.pomFile(parentArtifact));
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
		} catch (ArtifactNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Map<String,String> dependencyManagement = null;
	private String versionFor(String group, String artifact) {
		String version = null;
		if (dependencyManagement == null) {
			// Need to take all parent poms and traverse them to find
			// the dependency versions
			List<ArtifactImpl> parents = new ArrayList<ArtifactImpl>();
			ArtifactImpl parent = parentArtifact;
			while (parent != null) {
				parents.add(0,parent);
				parent = parent.parentArtifact;
			}
			for (ArtifactImpl a : parents) {
				File pomFile = this.repository.pomFile(a);
				try {
					InputStream is = pomFile.toURL().openStream();
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document document = builder.parse(is);
					is.close();
					List<Node> elementList = findElements(document, "dependencyManagement");
					for (Node depNode : elementList) {
						Node n = findElements(depNode, "groupId" ).iterator().next();
						String groupId = n.getFirstChild().getNodeValue().trim();
						n = findElements(depNode, "artifactId" ).iterator().next();
						String artifactId = n.getFirstChild().getNodeValue().trim();
						n = findElements(depNode, "version" ).iterator().next();
						version = n.getFirstChild().getNodeValue().trim();
						dependencyManagement.put(groupId+":"+artifactId,version);
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return dependencyManagement.get(group+":"+artifact);
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
			checkParent(pomFile);
			InputStream is;
			try {
				is = pomFile.toURL().openStream();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(is);
				is.close();
				List<Node> elementList = findElements(document, "dependency");
				for (Node node : elementList) {
					node.normalize();
					Node n = findElements(node, "groupId" ).iterator().next();
					String groupId = n.getFirstChild().getNodeValue().trim();
					n = findElements(node, "artifactId" ).iterator().next();
					String artifactId = n.getFirstChild().getNodeValue().trim();
					List<Node> versionNodeList = findElements(node, "version");
					String version = null;
					if (versionNodeList.isEmpty() == false) {
						n = findElements(node, "version" ).iterator().next();
						version = n.getFirstChild().getNodeValue().trim();
					}
					else {
						version = versionFor(groupId,artifactId);
					}
					if (version != null) {
						result.add(new ArtifactImpl(groupId, artifactId, version, this.repository));
					}	
					else {
						System.out.println("Warning - unable to find a version for the dependency "+groupId+":"+artifactId);
					}
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
