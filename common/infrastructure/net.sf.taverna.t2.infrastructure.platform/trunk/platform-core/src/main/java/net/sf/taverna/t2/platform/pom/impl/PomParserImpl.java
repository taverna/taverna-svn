package net.sf.taverna.t2.platform.pom.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

import net.sf.taverna.t2.platform.pom.ArtifactDescription;
import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import net.sf.taverna.t2.platform.pom.ArtifactParseException;
import net.sf.taverna.t2.platform.pom.PomParser;
import net.sf.taverna.t2.platform.util.download.DownloadException;
import net.sf.taverna.t2.platform.util.download.DownloadManager;
import net.sf.taverna.t2.platform.util.download.DownloadVerifier;
import net.sf.taverna.t2.platform.util.download.impl.Maven2MD5Verifier;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of the PomParser interface
 * <p>
 * At the moment this does not handle snapshot versions, but I'm not sure this
 * is a problem in reality - this only applies to explicitly managed snapshots
 * using the maven metadata xml files to redirect to a particular build and this
 * is not a likely deployment case for the artifacts we're going to be loading
 * from the platform. If it becomes critical we can always add it here later.
 * <p>
 * This class is intended for spring based instantiation, it has a default
 * constructor but you must set the download manager and base file cache
 * location properties before calling any of the 'real' methods here.
 * 
 * @author Tom Oinn
 * 
 */
public class PomParserImpl implements PomParser {

	private DownloadManager manager;
	private File cache;
	private DownloadVerifier verifier;

	// Pattern to recognize a variable insertion within a pom file
	private static Pattern propertyPattern = Pattern
			.compile("\\$\\{([^}]*)\\}");

	// Scopes which should not result in a dependency being added to either the
	// mandatory or optional dependency list
	private static String[] excludedScopes = new String[] { "test", "system",
			"provided" };

	private Map<ArtifactIdentifier, ArtifactDescription> descriptionCache = new HashMap<ArtifactIdentifier, ArtifactDescription>();

	/**
	 * Construct a new PomParserImpl, you must call the various set methods
	 * before this instance is usable.
	 */
	public PomParserImpl() {
		this.verifier = new Maven2MD5Verifier();
	}

	/**
	 * Inject a download manager to be used by this instance.
	 */
	public void setDownloadManager(DownloadManager dm) {
		this.manager = dm;
	}

	/**
	 * Set the file cache location for artifact files
	 */
	public void setFileCache(File cacheLocation) {
		this.cache = cacheLocation;
	}

	public ArtifactDescription getDescription(ArtifactIdentifier id,
			List<URL> repositories) throws ArtifactParseException {
		if (descriptionCache.containsKey(id)) {
			return descriptionCache.get(id);
		} else {
			return getDescriptionInternal(id, repositories);
		}
	}

	public synchronized ArtifactDescription getDescriptionInternal(
			ArtifactIdentifier id, List<URL> repositories)
			throws ArtifactParseException {
		// Check the cache first
		if (descriptionCache.containsKey(id)) {
			return descriptionCache.get(id);
		}

		List<ArtifactIdentifierDocumentPair> pomDocs = getPomDocuments(id,
				repositories);

		// Get the properties for this set of documents, including horrible
		// cases such as where properties in child poms interpolate values from
		// parents (gah, does anyone really do that?)
		Properties props = new Properties();

		// Collect dependency information on the fly as a side effect
		Map<String, String> dependencyManagement = new HashMap<String, String>();

		// Read in from highest ancestor first
		for (int i = pomDocs.size() - 1; i >= 0; i--) {
			Document pomDoc = pomDocs.get(i).doc;
			// Set the 'calculated' properties for the current pom, because
			// we're reading down to the one we actually want we'll end up with
			// the ones for the requested artifact but in the meantime we need
			// to set them for each pom in the ancestor tree
			ArtifactIdentifier currentArtifact = pomDocs.get(i).id;
			for (String prefix : new String[] { "", "pom.", "project." }) {
				props.put(prefix + "groupId", currentArtifact.getGroupId());
				props.put(prefix + "artifactId", currentArtifact
						.getArtifactId());
				props.put(prefix + "version", currentArtifact.getVersion());
			}
			NodeList propertyNodes = pomDoc.getElementsByTagName("properties");
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
			// Get dependency management elements while we still have the
			// correct properties block for this ancestor
			List<Node> managerElementList = DomUtils.findElements(pomDoc,
					"dependencyManagement");
			for (Node depManagerNode : managerElementList) {
				List<Node> elementList = DomUtils.findElements(depManagerNode,
						"dependency");
				for (Node depNode : elementList) {
					Node groupNode = DomUtils.findElements(depNode, "groupId")
							.iterator().next();
					String groupId = groupNode.getFirstChild().getNodeValue()
							.trim();
					groupId = expandProperties(props, groupId);
					Node artifactNode = DomUtils.findElements(depNode,
							"artifactId").iterator().next();
					String artifactId = artifactNode.getFirstChild()
							.getNodeValue().trim();
					artifactId = expandProperties(props, artifactId);
					Node versionNode = DomUtils
							.findElements(depNode, "version").iterator().next();
					String version = expandProperties(props, versionNode
							.getFirstChild().getNodeValue().trim());
					// Record the dependency management information in the map
					// as we go along
					dependencyManagement.put(groupId + ":" + artifactId,
							version);
				}
			}
		}

		// We now have the necessary dependency management and properties blocks
		// set up and configured, now to read the dependency information for
		// this particular artifact.
		List<ArtifactIdentifier> deps = new ArrayList<ArtifactIdentifier>();
		List<ArtifactIdentifier> optionalDeps = new ArrayList<ArtifactIdentifier>();
		// Exclusions specified as groupId:artifactId strings
		Set<String> exclusions = new HashSet<String>();
		List<Node> depNodeList = DomUtils.findElements(pomDocs.get(0).doc,
				new String[] { "project", "dependencies", "dependency" });
		for (Node depNode : depNodeList) {
			// Get dependency information, using version from dependency
			// management map if needed
			Node n = DomUtils.findElements(depNode, "groupId").iterator()
					.next();
			String groupId = n.getFirstChild().getNodeValue().trim();
			groupId = expandProperties(props, groupId);

			n = DomUtils.findElements(depNode, "artifactId").iterator().next();
			String artifactId = n.getFirstChild().getNodeValue().trim();
			artifactId = expandProperties(props, artifactId);

			List<Node> versionNodeList = DomUtils.findElements(depNode,
					"version");
			String version;
			if (versionNodeList.isEmpty()) {
				// If we get here we need to find the version from previously
				// scanned dependency management elements
				version = dependencyManagement.get(groupId + ":" + artifactId);
			} else {
				n = DomUtils.findElements(depNode, "version").iterator().next();
				version = n.getFirstChild().getNodeValue().trim();
				version = expandProperties(props, version);
			}
			if (version == null) {
				throw new ArtifactParseException(
						"Version is not defined for dependency '" + groupId
								+ ":" + artifactId + "'");
			}

			// Pull out any exclusion specifications - these are going to just
			// get munged into a single set and defined for the artifact as a
			// whole because there's no sensible way to reconcile the concept
			// with our one classloader per artifact approach in raven.
			List<Node> excludeNodes = DomUtils.findElements(depNode,
					new String[] { "exclusions", "exclusion" });
			if (!excludeNodes.isEmpty()) {
				for (Node excludeNode : excludeNodes) {
					Node groupNode = DomUtils.findElements(excludeNode,
							"groupId").iterator().next();
					String exGroupId = groupNode.getFirstChild().getNodeValue()
							.trim();
					Node artifactNode = DomUtils.findElements(excludeNode,
							"artifactId").iterator().next();
					String exArtifactId = artifactNode.getFirstChild()
							.getNodeValue().trim();
					exclusions.add(exGroupId + ":" + exArtifactId);
				}
			}

			// Figure out whether the dependency is optional or not
			boolean optional = false;
			List<Node> optionalNodeList = DomUtils.findElements(depNode,
					"optional");
			if (!optionalNodeList.isEmpty()) {
				n = optionalNodeList.get(0);
				String optionalString = n.getFirstChild().getNodeValue().trim();
				if (optionalString.equalsIgnoreCase("true")) {
					optional = true;
				}
			}

			// Test for scope, if scope is 'provided', 'system' or 'test' then
			// we don't add it as a dependency
			boolean downloadableScope = true;
			List<Node> scopeNodeList = DomUtils.findElements(depNode, "scope");
			if (!scopeNodeList.isEmpty()) {
				n = scopeNodeList.get(0);
				String scopeString = n.getFirstChild().getNodeValue().trim();
				for (String excludedScope : excludedScopes) {
					if (scopeString.equalsIgnoreCase(excludedScope)) {
						downloadableScope = false;
					}
				}
			}

			if (downloadableScope) {
				ArtifactIdentifier dependency = new ArtifactIdentifier(groupId,
						artifactId, version);
				if (optional) {
					optionalDeps.add(dependency);
				} else {
					deps.add(dependency);
				}
			}
		}
		ArtifactDescription description = new ArtifactDescriptionImpl(id, deps,
				optionalDeps);
		descriptionCache.put(id, description);
		return description;
	}

	private File getPomFile(ArtifactIdentifier id, List<URL> repositories) {
		return getFile(id, repositories, this.manager, this.verifier,
				this.cache, "pom");
	}

	/**
	 * Fetch the file for a specified artifact identifier. Uses local
	 * repositories if available first before trying non local repositories in
	 * order.
	 */
	public static File getFile(ArtifactIdentifier id, List<URL> repositories,
			DownloadManager manager, DownloadVerifier verifier, File cache,
			String extension) {
		// Check for local repositories, those with a file protocol in their
		// base URL
		List<URL> remoteLocations = new ArrayList<URL>();
		for (URL repository : repositories) {
			if (repository.getProtocol().equalsIgnoreCase("file")) {
				File repositoryFileBase = fileFromFileURL(repository);
				File file = new File(repositoryFileBase,
						pathFromArtifactIdentifier(id, extension));
				if (file.exists()) {
					return file;
				}
			} else {
				try {
					remoteLocations.add(new URL(repository,
							pathFromArtifactIdentifier(id, extension)));
				} catch (MalformedURLException mue) {
					throw new ArtifactParseException(
							"Can't construct remote file URL", mue);
				}
			}
		}
		try {
			return manager.getAsFile(remoteLocations, verifier,
					new ArtifactFileUrlMapper(cache, id), 1);
		} catch (DownloadException de) {
			if (id.isSnapshot()) {
				// Might be a 'real' snapshot in which case we need to do a bit
				// more
				try {
					String leafName = getSnapshotLeafName(id, repositories);
					// If we get here we have an appropriate metadata file and
					// can use it to get the real snapshot file location
					remoteLocations = new ArrayList<URL>();
					for (URL repository : repositories) {
						if (repository.getProtocol().equalsIgnoreCase("file")) {
							// Skip
						} else {
							try {
								remoteLocations.add(new URL(repository, id
										.getGroupId().replaceAll("\\.", "/")
										+ "/"
										+ id.getArtifactId()
										+ "/"
										+ id.getVersion()
										+ "/"
										+ leafName
										+ "." + extension));
							} catch (MalformedURLException mue) {
								//
							}
						}
					}
					return manager.getAsFile(remoteLocations, verifier,
							new ArtifactFileUrlMapper(cache, id), 1);
				} catch (DownloadException de2) {
					// Will fail here but fall through to the next throw clause
				}
			}
			throw new ArtifactParseException("Can't download file for '" + id
					+ "'", de);
		}

	}

	/**
	 * Assemble a list of documents containing the XML from the initially
	 * referenced artifact's pom at index 0 and its parents at subsequent
	 * indices.
	 * 
	 * @param id
	 *            the artifact to fetch pom documents for
	 * @param repositories
	 *            a list of local and remote repositories
	 * @return a list of Document instances with the artifact's pom at index 0
	 *         and any parents at subsequent indices in order in cases of
	 *         multiple ancestors
	 * @throws ArtifactParseException
	 *             if any problems are found parsing the pom files
	 */
	private List<ArtifactIdentifierDocumentPair> getPomDocuments(
			ArtifactIdentifier id, List<URL> repositories) {
		List<ArtifactIdentifierDocumentPair> result = new ArrayList<ArtifactIdentifierDocumentPair>();
		ArtifactIdentifier currentArtifact = id;
		while (currentArtifact != null) {
			File pomFile = getPomFile(currentArtifact, repositories);
			Document pomDoc = DomUtils.readXML(pomFile);
			result.add(new ArtifactIdentifierDocumentPair(currentArtifact,
					pomDoc));
			// Check to see if there's a parent reference in the current pom
			// document and, if so, re-queue the loop with the parent artifact
			// id.
			List<Node> elementList = DomUtils.findElements(pomDoc, "parent");
			if (elementList.isEmpty()) {
				currentArtifact = null;
			} else {
				Node parentNode = elementList.iterator().next();
				Node n = DomUtils.findElements(parentNode, "groupId")
						.iterator().next();
				String parentGroupId = n.getFirstChild().getNodeValue().trim();
				n = DomUtils.findElements(parentNode, "artifactId").iterator()
						.next();
				String parentArtifactId = n.getFirstChild().getNodeValue()
						.trim();
				n = DomUtils.findElements(parentNode, "version").iterator()
						.next();
				String parentVersion = n.getFirstChild().getNodeValue().trim();
				currentArtifact = new ArtifactIdentifier(parentGroupId,
						parentArtifactId, parentVersion);
			}
		}
		return result;
	}

	/**
	 * Annoying helper class because Java doesn't have on the fly tuple
	 * construction in its type system
	 */
	class ArtifactIdentifierDocumentPair {

		ArtifactIdentifier id;
		Document doc;

		public ArtifactIdentifierDocumentPair(ArtifactIdentifier id,
				Document doc) {
			this.id = id;
			this.doc = doc;
		}
	}

	/**
	 * Expand a string containing ${foo.bar} type property specifier from a
	 * Properties object with the 'foo.bar' property defined.
	 * 
	 * @return the modified or unmodified string, depending on whether the named
	 *         property was found
	 */
	private static String expandProperties(Properties properties, String string) {
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
				// Do nothing, undefined property so leave it as is, this is
				// probably a mistake somewhere but you never know!
			} else {
				newString = newString.replace(keyVariable, value);
			}
		}
		return newString;
	}

	/**
	 * Get a local file corresponding to a URL with the file: protocol
	 * 
	 * @param fileURL
	 * @return
	 */
	public static File fileFromFileURL(URL fileURL) {
		if (fileURL == null) {
			throw new RuntimeException("Can't get file for null URL");
		}
		try {
			return new File(fileURL.toURI());
		} catch (URISyntaxException e) {
			return new File(fileURL.getPath());
		}
	}

	/**
	 * Get the relative path within a repository for the given artifact - this
	 * currently has no special treatment for snapshot versions.
	 * 
	 * @param id
	 * @return
	 */
	private static String pathFromArtifactIdentifier(ArtifactIdentifier id,
			String extension) {
		return id.getGroupId().replaceAll("\\.", "/") + "/"
				+ id.getArtifactId() + "/" + id.getVersion() + "/"
				+ id.getArtifactId() + "-" + id.getVersion() + "." + extension;
	}

	/**
	 * Attempt to load a snapshot maven-metadata xml for a given artifact
	 * identifier from a list of repository locations
	 */
	private static String getSnapshotLeafName(ArtifactIdentifier id,
			List<URL> repositories) {
		for (URL repository : repositories) {
			if (repository.getProtocol().equalsIgnoreCase("file")) {
				// Skip
			} else {
				try {
					URL metadataLocation = new URL(repository, id.getGroupId()
							.replaceAll("\\.", "/")
							+ "/"
							+ id.getArtifactId()
							+ "/"
							+ id.getVersion()
							+ "/maven-metadata.xml");
					Document metadataDoc = DomUtils.readXML(metadataLocation);
					NodeList ts = metadataDoc.getElementsByTagName("timestamp");
					NodeList bn = metadataDoc
							.getElementsByTagName("buildNumber");
					String timestamp = "";
					String buildnumber = "";
					if (ts.getLength() > 0) {
						timestamp = ts.item(0).getTextContent();
					} else {
						throw new ArtifactParseException("No timestamp");
					}

					if (bn.getLength() > 0) {
						buildnumber = bn.item(0).getTextContent();
					} else {
						throw new ArtifactParseException("No build number");
					}
					return id.getArtifactId()
							+ "-"
							+ (id.getVersion().replaceAll("SNAPSHOT", timestamp
									+ "-" + buildnumber));
				} catch (MalformedURLException mue) {
					// Thrown by URL constructor
				} catch (ArtifactParseException ape) {
					// Thrown by the xml dom parser
				}
			}
		}
		throw new DownloadException(
				"Can't find or parse an appropriate metadata-xml for snapshot artifact "
						+ id);
	}
}
