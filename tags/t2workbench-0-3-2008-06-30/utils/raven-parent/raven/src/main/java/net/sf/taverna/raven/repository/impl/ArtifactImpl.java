package net.sf.taverna.raven.repository.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.util.NodeListIterable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A single Maven2 artifact with group, artifact and version
 * 
 * @author Tom Oinn
 * @author Danielle Turi
 * @author Stian Soiland-Reyes
 */
public class ArtifactImpl extends BasicArtifact {

	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
			.newInstance();
	
	private static Log logger = Log.getLogger(ArtifactImpl.class);

	/**
	 * Matches ${project.artifactId} etc.
	 */
	private static Pattern propertyPattern = Pattern.compile("\\$\\{([^}]*)\\}");

	private List<ArtifactImpl> dependencies = null;
	private Map<String, String> dependencyManagement = null;
	private String packageType = null;
	private ArtifactImpl parentArtifact = null;
	private Properties properties;
	private LocalRepository repository;

	protected Set<Artifact> exclusions = null;

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
			// ArtifactImpl with a null repository.
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

		File pomFile = getPomFile();
		if (pomFile == null) {
			// TODO Handle absence of pom file here
			return result;
		}
		checkParent(pomFile);

		try {
			Document document = readXML(pomFile);
			Properties properties = getProperties();

			List<Node> elementList = findElements(document, new String[] {
					"project", "dependencies", "dependency" });
			for (Node node : elementList) {
				node.normalize();
				Node n = findElements(node, "groupId").iterator().next();
				String groupId = n.getFirstChild().getNodeValue().trim();
				groupId = expandProperties(properties, groupId);

				n = findElements(node, "artifactId").iterator().next();
				String artifactId = n.getFirstChild().getNodeValue().trim();
				artifactId = expandProperties(properties, artifactId);

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
					version = expandProperties(properties, version);
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

	public String getPackageType() {
		if (packageType != null) {
			return packageType;
		}
		File pomFile = repository.pomFile(this);
		if (pomFile.exists()) {
			try {
				Document document = readXML(pomFile);
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
	 * Force all parent pom XML files to exist within the repository, set up the
	 * parentArtifact field if one is found
	 */
	private synchronized void checkParent(File pomFile) {
		Document document;
		try {
			try {
				document = readXML(pomFile);
			} catch (ParserConfigurationException ex) {
				logger.error("Could not create XML document builder", ex);
				return;
			} catch (SAXException e) {
				logger.warn("Could not parse XML " + pomFile, e);
				return;
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

	private String expandProperties(Properties properties, String string) {
		if (string == null) {
			throw new NullPointerException("Value can't be null");
		}
		Matcher matcher = propertyPattern.matcher(string);
		String newString = string;		
		while (matcher.find()) {
			String key = matcher.group(1);
			String keyVariable = "${" + key + "}";
			String value = properties.getProperty(key);
			if (value == null) {
				//logger.info("Can't resolve property " + key + " from " + this);
			} else {
				newString = newString.replace(keyVariable, value);
			}
		}
//		if (! string.equals(newString)) {
//			logger.debug("Expanded " + string + " to " + newString);
//		}
		return newString;
	}

	/**
	 * Find any descendants of the given node with the specified element name
	 * 
	 * @param fromnode
	 * @param name
	 * @return
	 */
	private List<Node> findElements(Node fromnode, String name) {
		List<Node> list = new ArrayList<Node>();
		for (Node node : new NodeListIterable(fromnode.getChildNodes())) {
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
	 * Return the list of ancestor, oldest parent first.
	 * 
	 * @return
	 */
	private List<ArtifactImpl> getAncestors() {
		List<ArtifactImpl> parents = new ArrayList<ArtifactImpl>();
		ArtifactImpl parent = parentArtifact;
		while (parent != null) {
			parents.add(0, parent);
			parent = parent.parentArtifact;
		}
		return parents;
	}

	/**
	 * Get maven properties calculated from the current POM, such as
	 * ${project.version}.
	 * 
	 * @return A new {@link Properties} object with calculated properties
	 */
	private Properties getCalculatedProperties() {
		// Loosely based on
		// http://docs.codehaus.org/display/MAVENUSER/MavenPropertiesGuide
		Properties properties = new Properties();
		for (String prefix : new String[] { "", "pom.", "project." }) {
			properties.put(prefix + "groupId", getGroupId());
			properties.put(prefix + "artifactId", getArtifactId());
			properties.put(prefix + "version", getVersion());
		}
		return properties;
	}

	private File getPomFile() {
		File pomFile = repository.pomFile(this);
		if (!pomFile.exists()) {
			logger.error("Pom file does not exist: " + pomFile);
			// TODO Handle absence of pom file here
			return null;
		}
		return pomFile;
	}

	/**
	 * Get the Maven properties defined, inherited and calculated within this
	 * POM. To be used with {@link #expandProperties(Properties, String)}.
	 * 
	 * @see #expandProperties(Properties, String)
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	@SuppressWarnings("unchecked")
	private Properties getProperties() throws IOException,
			MalformedURLException, ParserConfigurationException, SAXException {
		if (properties != null) {
			// Note - not synchronized - but no big harm in two threads reading
			// the properties
			// at the same time because we don't assing until the end
			return properties;
		}
		File pomFile = getPomFile();
		if (pomFile == null) {
			throw new IOException("Can't find pom file for " + this);
		}

		Properties props = getCalculatedProperties();
		if (parentArtifact != null) {
			props.putAll(parentArtifact.getProperties());
		}
		props.putAll(getCalculatedProperties());

		Document document = readXML(pomFile);
		NodeList propertyNodes = document.getElementsByTagName("properties");

		for (Node propertiesNode : new NodeListIterable(propertyNodes)) {
			for (Node item : new NodeListIterable(propertiesNode
					.getChildNodes())) {
				item.normalize();
				String textContent = item.getTextContent().trim();
				if (!textContent.equals("")) {
					String key = item.getNodeName();
					props.put(key, expandProperties(props, textContent));
				}
			}
		}
		properties = props;
		return props;
	}

	private Document readXML(File xmlFile) throws MalformedURLException,
			ParserConfigurationException, SAXException, IOException {
		return readXML(xmlFile.toURI().toURL());
	}

	private Document readXML(URL url) throws ParserConfigurationException,
			SAXException, IOException {
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		InputStream stream = url.openStream();
		Document document;
		try {
			document = builder.parse(stream);
		} finally {
			stream.close();
		}
		return document;
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

	private String versionFor(String group, String artifact) {
		String version = null;
		if (dependencyManagement == null) {
			// Need to take all parent poms and traverse them to find
			// the dependency versions
			dependencyManagement = new HashMap<String, String>();
			for (ArtifactImpl ancestor : getAncestors()) {
				File pomFile = repository.pomFile(ancestor);
				try {
					Document document = readXML(pomFile);
					Properties ancestorProperties = ancestor.getProperties();
					List<Node> managerElementList = findElements(document,
							"dependencyManagement");
					for (Node depManagerNode : managerElementList) {
						List<Node> elementList = findElements(depManagerNode,
								"dependency");
						for (Node depNode : elementList) {
							Node groupNode = findElements(depNode, "groupId")
									.iterator().next();
							String groupId = groupNode.getFirstChild()
									.getNodeValue().trim();
							groupId = expandProperties(ancestorProperties,
									groupId);

							Node artifactNode = findElements(depNode,
									"artifactId").iterator().next();
							String artifactId = artifactNode.getFirstChild()
									.getNodeValue().trim();
							artifactId = expandProperties(ancestorProperties,
									artifactId);

							Node versionNode = findElements(depNode, "version")
									.iterator().next();
							version = versionNode.getFirstChild()
									.getNodeValue().trim();
							version = expandProperties(ancestorProperties,
									version);
							dependencyManagement.put(
									groupId + ":" + artifactId, version);
						}
					}

				} catch (IOException e) {
					logger.warn("IO error reading " + ancestor, e);
				} catch (ParserConfigurationException e) {
					logger.error("XML parser configuration error", e);
				} catch (SAXException e) {
					logger.warn("XML SAX error reading " + ancestor, e);
				}
			}
		}
		return dependencyManagement.get(group + ":" + artifact);
	}

}
