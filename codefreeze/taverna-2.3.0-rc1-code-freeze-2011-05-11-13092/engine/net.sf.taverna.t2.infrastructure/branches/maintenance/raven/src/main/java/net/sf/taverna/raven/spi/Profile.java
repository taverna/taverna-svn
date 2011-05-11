/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.raven.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.ArtifactImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A Profile in this context is a set of Artifacts that are known to work in
 * conjunction with one another. With Raven's ability to deploy components at
 * runtime and update in a very fine grained manner the issue of support can
 * become tangled, the potential variety of coexisting (and therefore possibly
 * interacting) software components may cause incompatibilities that are only
 * apparent at runtime. For this reason some organisations such as OMII-UK may
 * with to provide a 'blessed' combination of component versions which have had
 * some level of integration testing within their host environment.
 * <p>
 * The profile is held and distributed in the form of an XML file with the
 * following structure:
 * 
 * <pre>
 *  &lt;profile&gt;
 *    &lt;artifact groupId=&quot;...&quot; artifactId=&quot;...&quot; version=&quot;...&quot;/&gt;
 *    ...
 *  &lt;/profile&gt;
 * </pre>
 * 
 * Note that as this is only used by the SPI mechanism there is no need to
 * include dependencies of these artifacts, the only entries required are those
 * which directly contain SPI implementations
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 */
public class Profile extends AbstractArtifactFilter {
	private static Log logger = Log.getLogger(Profile.class);

	private Set<Artifact> artifacts = new HashSet<Artifact>();
	private Set<Artifact> systemArtifacts = new HashSet<Artifact>();

	private boolean strict;

	private String version;
	private String name;

	private Map<List<Object>, Artifact> discoverArtifactCache = new Hashtable<List<Object>, Artifact>();

	public Profile(boolean strict) {
		this.strict = strict;
	}

	/**
	 * Create a Profile and initialize it from the specified InputStream of XML
	 * (see class description)
	 * <p>
	 * If the strict setting is set to true then the filter operation is a
	 * straight set intersection of the set to be filtered and the set of
	 * artifacts within this profile. If false then the behaviour is slightly
	 * more complex - an artifact is allowed through the filter if either all
	 * three fields (groupId, artifactId and version) match or there are no
	 * matches on the groupId and artifactId pair. This effectively allows
	 * through components which are unknown to the profile and can be used to
	 * compose the union of multiple profiles by adding each one to the filter
	 * chain in turn.
	 * 
	 * @param is
	 *            {@link InputStream} to read XML from
	 * @param strict
	 *            only allows exact matches to the profile through if true, if
	 *            false then artifacts which don't exist in the profile in any
	 *            version will be allowed through.
	 * @throws InvalidProfileException
	 *             if there is any problem reading or parsing the profile XML.
	 */
	public Profile(InputStream is, boolean strict)
			throws InvalidProfileException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document;
		this.strict = strict;

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(is);
			} catch (SAXException e) {
				throw new InvalidProfileException(
						"Unable to parse profile XML", e);
			} catch (IOException e) {
				throw new InvalidProfileException("Unable to open profile XML",
						e);
			}
		} catch (ParserConfigurationException e) {
			throw new InvalidProfileException("Failed to create XML parser", e);
		}

		// determine the version if available
		Node profileVersionAttribute = document.getDocumentElement()
				.getAttributes().getNamedItem("version");
		if (profileVersionAttribute != null) {
			version = profileVersionAttribute.getNodeValue();
		} else {
			logger.warn("Profile document contains no version.");
			version = null;
		}

		// determine the name if available
		Node profileNameAttribute = document.getDocumentElement()
				.getAttributes().getNamedItem("name");
		if (profileNameAttribute != null) {
			name = profileNameAttribute.getNodeValue();
		} else {
			logger.warn("Profile document contains no name.");
			name = null;
		}

		NodeList nodelist = document.getDocumentElement().getChildNodes();
		readProfileArtifactNodes(nodelist);
	}

	/**
	 * Allow an artifact to be added to the profile at runtime
	 * 
	 * @param artifact
	 */
	public void addArtifact(Artifact artifact) {
		artifacts.add(artifact);
		fireFilterChanged(this);
		discoverArtifactCache.clear();
	}

	/**
	 * Adds artifacts contains in the profile definition for a Plugin. These
	 * need to be added early in the startup process so that system artifacts
	 * get added to the correct classloader. It also prevents a long delay
	 * between the splash screen and the workbench appearing when first
	 * initialising default plugins.
	 * 
	 * @param pluginsDefinitionStream
	 */
	public void addArtifactsForPlugins(InputStream pluginsDefinitionStream) {
		Document document;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(pluginsDefinitionStream);
				NodeList pluginNodes = document.getElementsByTagName("plugin");
				for (int i = 0; i < pluginNodes.getLength(); i++) {
					Node node = pluginNodes.item(i);
					Node child = node.getFirstChild();
					Node profileNode = null;
					boolean enabled = false;
					while (child != null) {
						if (child.getNodeName().equals("enabled")) {
							enabled = child.getTextContent() != null
									&& child.getTextContent().trim().equals(
											"true");
						}
						if (child.getNodeName().equals("profile")) {
							profileNode = child;
						}
						child = child.getNextSibling();
					}
					if (enabled && profileNode != null) {
						readProfileArtifactNodes(profileNode.getChildNodes());
					}
				}
			} catch (SAXException e) {
				logger.error("Error reading plugin xml", e);
			} catch (IOException e) {
				logger.error("Error reading plugin xml stream", e);
			} catch (InvalidProfileException e) {
				logger.error("The plugin xml contains an invalid profile", e);
			}
		} catch (ParserConfigurationException e) {
			logger.error("The XML parser is incorrectly configured", e);
		}
	}

	// new
	public void addSystemArtifact(Artifact artifact) {
		systemArtifacts.add(artifact);
		fireFilterChanged(this);
	}

	/**
	 * Select the highest version {@link Artifact} defined in the registry that
	 * fits the artifactId and groupId. Useful for allowing artifacts to be
	 * defined without version with the profile dictating the version to be
	 * used.
	 * <p>
	 * Versions are compared as described in {@link VersionComparator}.
	 * 
	 * @see VersionComparator
	 * @param groupId
	 * @param artifactId
	 * @return the Artifact or null if not found
	 */
	public Artifact discoverArtifact(String groupId, String artifactId) {
		return discoverArtifact(groupId, artifactId, null);
	}

	/**
	 * Select the highest version {@link Artifact} defined in the registry that
	 * fits the artifactId and groupId. Useful for allowing artifacts to be
	 * defined without version with the profile dictating the version to be
	 * used.
	 * <p>
	 * Versions are compared as described in {@link VersionComparator}.
	 * 
	 * @see VersionComparator
	 * @param groupId
	 * @param artifactId
	 * @param repository
	 *            Repository to use for finding nested dependencies
	 * @return the Artifact or null if not found
	 */
	public Artifact discoverArtifact(String groupId, String artifactId,
			Repository repository) {
		List<Object> cacheKey = Arrays.asList(groupId, artifactId, repository);
		Artifact result = discoverArtifactCache.get(cacheKey);
		if (result != null) {
			return result;
		}
		
		List<Artifact> matches = new ArrayList<Artifact>();

		// The profile will decide if it states the artifact directly
		for (Artifact artifact : artifacts) {
			if (artifact.getArtifactId().equals(artifactId)
					&& artifact.getGroupId().equals(groupId)) {
				matches.add(artifact);
			}
		}
		if (matches.isEmpty() && repository != null) {
			// We'll need to dig deeper

			// Build a map to quickly look up ArtifactImpl's
			Map<Artifact, ArtifactImpl> repArtifacts = new HashMap<Artifact, ArtifactImpl>();
			Set<Artifact> search = new HashSet<Artifact>();
			for (Artifact artifact : repository.getArtifacts()) {
				if (!(artifact instanceof ArtifactImpl)) {
					logger.warn("Not instance of ArtifactImpl: " + artifact);
					continue;
				}
				repArtifacts.put(artifact, (ArtifactImpl) artifact);
				search.add(artifact);
			}
			
			Set<Artifact> visitted = new HashSet<Artifact>();
			
			while (!search.isEmpty()) {
				Artifact artifact = search.iterator().next();
				search.remove(artifact);
				visitted.add(artifact);

				if (artifact.getArtifactId().equals(artifactId)
						&& artifact.getGroupId().equals(groupId)) {
					matches.add(artifact);
				} else {
					// Search it's children 
					ArtifactImpl artifactImpl = repArtifacts.get(artifact);
					if (artifactImpl == null) {
						logger.warn("Unknown artifact " + artifact);
						continue;
					}
					Set<Artifact> newDependencies;
					try {
						newDependencies = new HashSet<Artifact>(artifactImpl
								.getDependencies());
					} catch (ArtifactStateException e) {
						logger.warn("Invalid state for artifact: " + artifact);
						continue;
					}
					newDependencies.removeAll(visitted);
					search.addAll(newDependencies);
				}
			}
		}
		if (!matches.isEmpty()) {
			VersionComparator.sort(matches);
			// return LAST element (ie. highest version)
			result = matches.get(matches.size() - 1);
		}
		if (result != null) {
			discoverArtifactCache.put(cacheKey, result);
		}
		return result;
	}

	/**
	 * Return the intersection of the set of Artifacts in this Profile and that
	 * presented to this method if strict is true, otherwise return the
	 * intersection plus all artifacts in the set which have no match within the
	 * profile when only groupId and artifactId are taken into account.
	 * 
	 * @return filtered list of Artifact objects
	 */
	public Set<Artifact> filter(Set<Artifact> intersecting) {
		Set<Artifact> result = new HashSet<Artifact>();
		for (Artifact artifact : intersecting) {
			if (artifacts.contains(artifact)) {
				// Exact match to an entry in the profile so include it
				result.add(artifact);
			} else if (!strict) {
				// We will include the artifact as long as we don't have it in
				// another version
				if (!containsOtherVersion(artifact)) {
					result.add(artifact);
				}
			}
		}
		return result;
	}

	/**
	 * Get the artifacts that forms part of this profile.
	 * 
	 * @return a copy of the internal {@link Set} of {@link Artifact}.
	 */
	public Set<Artifact> getArtifacts() {
		return new HashSet<Artifact>(artifacts);
	}

	/**
	 * Return the name of the profile, or null if no name is defined
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the subset of {@link #getArtifacts()} that is marked as being system
	 * artifacts by this profile. A system artifact is supposed to be added by
	 * {@link net.sf.taverna.raven.prelauncher.PreLauncher#addURLToClassPath(java.net.URL)}
	 * and thereby available even to artifacts that don't declare it as a
	 * dependency. This is mainly useful for global XML parsers and similar
	 * implementations that are discovered by non-Raven SPIs.
	 * 
	 * @return a copy of the internal Set of system Artifacts
	 */
	public Set<Artifact> getSystemArtifacts() {
		return new HashSet<Artifact>(systemArtifacts);
	}

	/**
	 * Return the version string of the Profile, or 'NO-VERSION' if a version is
	 * not defined
	 */
	public String getVersion() {
		if (version == null) {
			return "NO-VERSION";
		}
		return version;
	}

	/**
	 * Allow an artifact to be removed from the profile at runtime
	 * 
	 * @param artifact
	 */
	public void removeArtifact(Artifact artifact) {
		artifacts.remove(artifact);
		systemArtifacts.remove(artifacts);
		discoverArtifactCache.clear();
		fireFilterChanged(this);
	}

	// new (probably not needed)
	public void removeSystemArtifact(Artifact artifact) {
		systemArtifacts.remove(artifact);
		fireFilterChanged(this);
	}

	/**
	 * Generate the XML representation of this profile, write to given output
	 * stream.
	 * 
	 * @param outputStream
	 *            Stream to output profile as XML.
	 * 
	 * @throws ParserConfigurationException
	 *             If a DocumentBuilder could not be created
	 * @throws TransformerFactoryConfigurationError
	 *             If a Transformer could not be created
	 * @throws TransformerException
	 *             If the XML document could not be transformed
	 */
	public void write(OutputStream outputStream)
			throws ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element element = doc.createElement("profile");
		doc.appendChild(element);
		if (version != null) {
			element.setAttribute("version", version);
		}
		if (name != null) {
			element.setAttribute("name", name);
		}
		for (Artifact artifact : getArtifacts()) {
			String groupId = artifact.getGroupId();
			String artifactId = artifact.getArtifactId();
			String version = artifact.getVersion();

			Element artifactElement = doc.createElement("artifact");
			artifactElement.setAttribute("groupId", groupId);
			artifactElement.setAttribute("artifactId", artifactId);
			artifactElement.setAttribute("version", version);
			if (systemArtifacts.contains(artifact)) {
				artifactElement.setAttribute("system", "true");
			}
			element.appendChild(artifactElement);
		}

		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult dest = new StreamResult(outputStream);
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(source, dest);
	}

	/**
	 * @param artifact
	 *            Artifact to look for ignoring version information
	 * @return whether there is a matching pair of groupId,artifactId in this
	 *         profile
	 */
	private boolean containsOtherVersion(Artifact artifact) {
		for (Artifact existing : artifacts) {
			if (existing.getArtifactId().equals(artifact.getArtifactId())
					&& existing.getGroupId().equals(artifact.getGroupId())) {
				return true;
			}
		}
		return false;
	}

	private void readProfileArtifactNodes(NodeList nodelist)
			throws InvalidProfileException {
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node n = nodelist.item(i);
			if (n instanceof Element) {
				NamedNodeMap atts = n.getAttributes();
				Node gnode = atts.getNamedItem("groupId");
				Node anode = atts.getNamedItem("artifactId");
				Node vnode = atts.getNamedItem("version");
				if (gnode == null || anode == null || vnode == null) {
					throw new InvalidProfileException(
							"Entries must contain groupId, artifactId, version");
				}
				Artifact artifact = new BasicArtifact(gnode.getNodeValue(),
						anode.getNodeValue(), vnode.getNodeValue());
				artifacts.add(artifact);
				Node snode = atts.getNamedItem("system");
				if (snode != null && "true".equals(snode.getNodeValue())) {
					systemArtifacts.add(artifact);
				}
			}
		}
	}

}
