package net.sf.taverna.raven.repository.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A single Maven2 artifact with group, artifact and version
 * 
 * @author Tom
 * @author dturi
 */
public class ArtifactImpl extends BasicArtifact {
	private static Log logger = Log.getLogger(ArtifactImpl.class);

	private LocalRepository repository;
	private String packageType = null;
	protected Set<Artifact> exclusions = null;
	private Map<String, String> dependencyManagement = null;
	private ArtifactImpl parentArtifact = null;
	private List<ArtifactImpl> dependencies = null;

	/**
	 * Create a new Artifact description
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param repository
	 */
	ArtifactImpl(String groupId, String artifactId, String version,
			LocalRepository repository) {
		super(groupId, artifactId, version);
		this.repository = repository;
	}

	/**
	 * Create a new ArtifactImpl from an Artifact and a Repository
	 */
	ArtifactImpl(Artifact a, LocalRepository repository) {
		super(a);
		this.repository = repository;
		if (a instanceof ArtifactImpl) {
			ArtifactImpl other = (ArtifactImpl) a;
			if (other.exclusions != null) {
				setExclusions(new HashSet<Artifact>(other.exclusions));
			}
		}
	}

	/**
	 * Analyse the corresponding .pom and return a list of all immediate
	 * dependencies from this artifact. If no repository is defined then return
	 * an empty list.
	 * 
	 * @return List of Artifacts upon which this depends
	 */
	public synchronized List<ArtifactImpl> getDependencies()
			throws ArtifactStateException {
		if (dependencies != null) {
			return dependencies;
		}
		List<ArtifactImpl> result = new ArrayList<ArtifactImpl>();
		if (repository == null) {
			logger.error("Repository is null");
			// Should never get here, it's impossible to construct an
			// ArtifactImpl with
			// a null repository.
			return result;
		}
		ArtifactStatus status = repository.getStatus(this);
		if (status.getOrder() < ArtifactStatus.Pom.getOrder()
				|| (status.isError() && !status
						.equals(ArtifactStatus.PomNonJar))) {
			throw new ArtifactStateException(status, new ArtifactStatus[] {
					ArtifactStatus.Analyzed, ArtifactStatus.Jar,
					ArtifactStatus.Pom, ArtifactStatus.Ready });
		}

		File pomFile = repository.pomFile(this);
		if (!pomFile.exists()) {
			logger.error("Pom file does not exist: " + pomFile);
			// TODO Handle absence of pom file here
			return result;
		}
		checkParent(pomFile);
		InputStream is;
		try {
			is = pomFile.toURI().toURL().openStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(is);
			is.close();

			Properties properties = getProperties(pomFile);

			List<Node> elementList = findElements(document, new String[] {
					"project", "dependencies", "dependency" });
			for (Node node : elementList) {
				node.normalize();
				Node n = findElements(node, "groupId").iterator().next();
				String groupId = n.getFirstChild().getNodeValue().trim();
				if (groupId.equals("${pom.groupId}")) {
					groupId = getGroupId();
				}
				n = findElements(node, "artifactId").iterator().next();
				String artifactId = n.getFirstChild().getNodeValue().trim();
				if (artifactId.equals("${pom.artifactId}")) {
					artifactId = getArtifactId();
				}

				// Check if we should exclude it
				if (exclusions != null
						&& exclusions.contains(new BasicArtifact(groupId,
								artifactId, ""))) {
					continue;
				}

				List<Node> versionNodeList = findElements(node, "version");
				String version;
				if (versionNodeList.isEmpty()) {
					version = versionFor(groupId, artifactId);
				} else {
					n = findElements(node, "version").iterator().next();
					version = n.getFirstChild().getNodeValue().trim();
					version = interpolate(properties, version);
				}
				if (version == null) {
					logger.warn("Unable to find a version for the dependency "
							+ groupId + ":" + artifactId + " - skipping");
					continue;
				}

				// Find exclusions (and inherit our own)
				Set<Artifact> depExclusions;
				if (exclusions != null) {
					depExclusions = new HashSet<Artifact>(exclusions);
				} else {
					depExclusions = new HashSet<Artifact>();
				}
				List<Node> excludeNodes = findElements(node, new String[] {
						"exclusions", "exclusion" });
				if (!excludeNodes.isEmpty()) {
					for (Node excludeNode : excludeNodes) {
						Node groupNode = findElements(excludeNode, "groupId")
								.iterator().next();
						String exGroupId = groupNode.getFirstChild()
								.getNodeValue().trim();
						Node artifactNode = findElements(excludeNode,
								"artifactId").iterator().next();
						String exArtifactId = artifactNode.getFirstChild()
								.getNodeValue().trim();
						BasicArtifact exclusion = new BasicArtifact(exGroupId,
								exArtifactId, "");
						// logger.info("Excluding " + exclusion);
						depExclusions.add(exclusion);
					}
				}

				// Check for optional dependency
				boolean optional = false;
				List<Node> optionalNodeList = findElements(node, "optional");
				if (!optionalNodeList.isEmpty()) {
					n = optionalNodeList.get(0);
					String optionalString = n.getFirstChild().getNodeValue()
							.trim();
					if (optionalString.equalsIgnoreCase("true")) {
						optional = true;
					}
				}

				// Test for scope, if scope is 'provided' or 'test' then
				// we don't add it as a dependency as this would force a
				// download.
				boolean downloadableScope = true;
				List<Node> scopeNodeList = findElements(node, "scope");
				if (!scopeNodeList.isEmpty()) {
					n = scopeNodeList.get(0);
					String scopeString = n.getFirstChild().getNodeValue()
							.trim();
					if (scopeString.equalsIgnoreCase("test")
							|| scopeString.equalsIgnoreCase("provided")
							|| scopeString.equalsIgnoreCase("system")) {
						downloadableScope = false;
					}
				}

				if (!optional && downloadableScope) {
					ArtifactImpl dependency = new ArtifactImpl(groupId,
							artifactId, version, repository);
					if (!depExclusions.isEmpty()) {
						dependency.setExclusions(depExclusions);
					}
					result.add(dependency);
				} else {
					// Log the optional dependency here if needed
				}
			}
			// FIXME: catching exceptions where they are thrown
		} catch (MalformedURLException e) {
			logger.error("Malformed URL", e);
		} catch (IOException e) {
			logger.warn("IO error", e);
		} catch (ParserConfigurationException e) {
			logger.error("XML parser configuration error", e);
		} catch (SAXException e) {
			logger.warn("XML SAX error", e);
		}
		dependencies = result;
		return result;
	}

	private String interpolate(Properties properties, String version) {
		if (version.startsWith("${") && version.endsWith("}")) {
			String versionKey = version.substring(2, version.length() - 1);
			if (versionKey.equals("pom.version")) {
				return getVersion();
			}
			version = properties.getProperty(versionKey);
		}
		return version;
	}

	@SuppressWarnings("unchecked")
	private Properties getProperties(File pomFile) throws IOException,
			MalformedURLException, ParserConfigurationException, SAXException {
		Properties properties = new Properties();
		InputStream is;
		is = pomFile.toURI().toURL().openStream();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(is);
		is.close();
		NodeList propertyNodes = document.getElementsByTagName("properties");
		for (int j = 0; j < propertyNodes.getLength(); j++) {
			Node propertiesNode = propertyNodes.item(j);
			NodeList list = propertiesNode.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node item = list.item(i);
				item.normalize();
				String textContent = item.getTextContent().trim();
				if (!textContent.equals("")) {
					String key = item.getNodeName();
					properties.put(key, textContent);
				}
			}
		}
		return properties;
	}

	public String getPackageType() {
		if (packageType != null) {
			return packageType;
		}
		File pomFile = repository.pomFile(this);
		if (pomFile.exists()) {
			InputStream is;
			try {
				is = pomFile.toURI().toURL().openStream();
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(is);
				List<Node> l = findElements(document, "packaging");
				if (l.isEmpty()) {
					packageType = "jar";
					return packageType;
				}
				Node packageNode = l.iterator().next();
				packageType = packageNode.getFirstChild().getNodeValue().trim();
				return packageType;
				// FIXME: catching exceptions where they are thrown
			} catch (MalformedURLException e) {
				logger.error("Malformed URL", e);
			} catch (IOException e) {
				logger.warn("IO error", e);
			} catch (ParserConfigurationException e) {
				logger.error("XML parser configuration error", e);
			} catch (SAXException e) {
				logger.warn("XML SAX error", e);
			}
		}
		return null;
	}

	/**
	 * Set the exclusions for this artifact.
	 * <p>
	 * The exclusions consists of a set of Artifacts, but with a "version" field
	 * set to "". When calculating <code>getDependencies()</code> the excluded
	 * artifacts will not be included. Additionally, this list of exclusions
	 * will be inherited down as exclusions for the dependencies that are found.
	 * <p>
	 * This comes from the <code>&lt;exclusions&gt;</code> block of the
	 * <code>.pom</code> file.
	 * 
	 * @param exclusions
	 */
	private void setExclusions(Set<Artifact> exclusions) {
		if (exclusions.isEmpty()) {
			exclusions = null;
		}
		this.exclusions = exclusions;
	}

	/**
	 * Force all parent pom XML files to exist within the repository, set up the
	 * parentArtifact field if one is found
	 */
	private synchronized void checkParent(File pomFile) {
		InputStream is;
		Document document;
		try {
			is = pomFile.toURI().toURL().openStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException ex) {
				logger.error("Could not create XML document builder", ex);
				return;
			}
			try {
				document = builder.parse(is);
			} catch (SAXException e) {
				logger.warn("Could not parse XML " + pomFile, e);
				return;
			} finally {
				is.close();
			}
		} catch (IOException e) {
			logger.warn("Could not read " + pomFile, e);
			return;
		}
		List<Node> elementList = findElements(document, "parent");
		if (elementList.isEmpty()) {
			return;
		}
		Node parentNode = elementList.iterator().next();
		Node n = findElements(parentNode, "groupId").iterator().next();
		String parentGroupId = n.getFirstChild().getNodeValue().trim();
		n = findElements(parentNode, "artifactId").iterator().next();
		String parentArtifactId = n.getFirstChild().getNodeValue().trim();
		n = findElements(parentNode, "version").iterator().next();
		String parentVersion = n.getFirstChild().getNodeValue().trim();
		parentArtifact = new ArtifactImpl(parentGroupId, parentArtifactId,
				parentVersion, repository);
		repository.addArtifact(parentArtifact);
		if (repository.getStatus(parentArtifact).equals(ArtifactStatus.Queued)) {
			try {
				// Force a fetch of the pom file
				repository.forcePom(parentArtifact);
			} catch (ArtifactNotFoundException e) {
				logger.warn("Could not fetch pom for artifact "
						+ parentArtifact, e);
				return;
			}
		}
		parentArtifact.checkParent(repository.pomFile(parentArtifact));
	}

	private String versionFor(String group, String artifact) {
		String version = null;
		if (dependencyManagement == null) {
			// Need to take all parent poms and traverse them to find
			// the dependency versions
			dependencyManagement = new HashMap<String, String>();
			List<ArtifactImpl> parents = new ArrayList<ArtifactImpl>();
			ArtifactImpl parent = parentArtifact;
			while (parent != null) {
				parents.add(0, parent);
				parent = parent.parentArtifact;
			}
			for (ArtifactImpl a : parents) {
				File pomFile = repository.pomFile(a);
				try {
					InputStream is = pomFile.toURI().toURL().openStream();
					DocumentBuilderFactory factory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document document = builder.parse(is);
					is.close();

					Properties properties = getProperties(pomFile);

					List<Node> managerElementList = findElements(document,
							"dependencyManagement");
					for (Node depManagerNode : managerElementList) {
						List<Node> elementList = findElements(depManagerNode,
								"dependency");
						for (Node depNode : elementList) {
							Node n = findElements(depNode, "groupId")
									.iterator().next();
							String groupId = n.getFirstChild().getNodeValue()
									.trim();
							if (groupId.equals("${pom.groupId}")) {
								groupId = a.getGroupId();
							}
							n = findElements(depNode, "artifactId").iterator()
									.next();
							String artifactId = n.getFirstChild()
									.getNodeValue().trim();
							if (artifactId.equals("${pom.artifactId}")) {
								artifactId = a.getArtifactId();
							}
							n = findElements(depNode, "version").iterator()
									.next();
							version = n.getFirstChild().getNodeValue().trim();
							version = interpolate(properties, version);
							/**
							 * logger.debug(this); logger.debug("Parent : "+a);
							 * logger.debug(groupId); logger.debug(artifactId);
							 * logger.debug(version);
							 */
							dependencyManagement.put(
									groupId + ":" + artifactId, version);
						}
					}

				} catch (IOException e) {
					logger.warn("IO error reading " + a, e);
				} catch (ParserConfigurationException e) {
					logger.error("XML parser configuration error", e);
				} catch (SAXException e) {
					logger.warn("XML SAX error reading " + a, e);
				}
			}
		}
		return dependencyManagement.get(group + ":" + artifact);
	}

	/**
	 * Find any descendants of the given node with the specified element name
	 * 
	 * @param fromnode
	 * @param name
	 * @return
	 */
	private List<Node> findElements(Node fromnode, String name) {
		NodeList nodelist = fromnode.getChildNodes();
		List<Node> list = new ArrayList<Node>();
		for (int i = 0; i < nodelist.getLength(); i++) {
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

	/**
	 * Find all immediate children of the given node with the specified element
	 * name
	 * 
	 * @param fromnode
	 * @param name
	 * @return
	 */
	private List<Node> findImmediateElements(Node fromnode, String name) {
		NodeList nodelist = fromnode.getChildNodes();
		List<Node> list = new ArrayList<Node>();
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (name.equals(node.getNodeName())) {
					list.add(node);
				}
				// list.addAll(findElements(node, name));
			}
		}
		return list;
	}

	/**
	 * Find all descendants of the given node with the specified sequence of
	 * element names - must be an exact match for the path from the specified
	 * node to a found node with the names of elements in the path corresponding
	 * to those in the names[] array.
	 * 
	 * @param fromNode
	 * @param names
	 * @return
	 */
	private List<Node> findElements(Node fromNode, String[] names) {
		List<Node> fromNodes = new ArrayList<Node>();
		fromNodes.add(fromNode);
		List<Node> foundNodes = new ArrayList<Node>();
		for (String name : names) {
			for (Node from : fromNodes) {
				foundNodes.addAll(findImmediateElements(from, name));
			}
			fromNodes = foundNodes;
			foundNodes = new ArrayList<Node>();
		}
		return fromNodes;
	}

}
