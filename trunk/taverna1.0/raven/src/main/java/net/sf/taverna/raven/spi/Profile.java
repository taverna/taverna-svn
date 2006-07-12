package net.sf.taverna.raven.spi;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;

/**
 * A Profile in this context is a set of Artifacts that are known to work in conjunction
 * with one another. With Raven's ability to deploy components at runtime and update
 * in a very fine grained manner the issue of support can become tangled, the potential
 * variety of coexisting (and therefore possibly interacting) software components may
 * cause incompatibilities that are only apparent at runtime. For this reason some
 * organizations such as OMII-UK may with to provide a 'blessed' combination of component
 * versions which have had some level of integration testing within their host environment.<p>
 * The profile is held and distributed in the form of an XML file with the following
 * structure:
 * <pre>
 * &lt;profile>
 *   &lt;artifact groupId="..." artifactId="..." version="..."/>
 *   ...
 * &lt;/profile>
 * </pre>
 * Note that as this is only used by the SPI mechanism there is no need to include dependencies
 * of these artifacts, the only entries required are those which directly contain SPI implementations
 * @author Tom Oinn
 *
 */
public class Profile implements ArtifactFilter {

	private Set<Artifact> artifacts = new HashSet<Artifact>();
	private boolean strict;
	
	/**
	 * Create a Profile and initialize it from the specified
	 * InputStream of XML (see class description)<p>
	 * If the strict setting is set to true then the filter
	 * operation is a straight set intersection of the set to be
	 * filtered and the set of artifacts within this profile. If
	 * false then the behaviour is slightly more complex - an artifact
	 * is allowed through the filter if either all three fields (
	 * groupId, artifactId and version) match or there are no
	 * matches on the groupId and artifactId pair. This effectively
	 * allows through components which are unknown to the profile and
	 * can be used to compose the union of multiple profiles by 
	 * adding each one to the filter chain in turn.
	 * @param is Inputstream to read XML from
	 * @param strict only allows exact matches to the profile through
	 * if true, if false then artifacts which don't exist in the profile
	 * in any version will be allowed through.
	 * @throws InvalidProfileException if there is any problem reading
	 * or parsing the profile XML.
	 */
	public Profile(InputStream is, boolean strict) throws InvalidProfileException {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document;		
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(is);
			} catch (SAXException e) {
				throw new InvalidProfileException("Unable to parse profile XML", e);
			} catch (IOException e) {
				throw new InvalidProfileException("Unable to open profile XML", e);
			}						
		} catch (ParserConfigurationException e) {
			throw new InvalidProfileException("Failed to create XML parser", e);
		}
		
		NodeList nodelist = document.getDocumentElement().getChildNodes();
		for (int i=0; i<nodelist.getLength(); i++) {
			Node n = nodelist.item(i);
			if (n instanceof Element) {
				NamedNodeMap atts = n.getAttributes();
				Node gnode = atts.getNamedItem("groupId");
				Node anode = atts.getNamedItem("artifactId");
				Node vnode = atts.getNamedItem("version");
				if (gnode == null || anode == null || vnode == null) {
					throw new InvalidProfileException("Entries must contain group, artifact, version");
				}
				Artifact a = new BasicArtifact(gnode.getNodeValue(),
						anode.getNodeValue(),
						vnode.getNodeValue());
				artifacts.add(a);
			}
		}
		
	}
	
	/**
	 * @return a copy of the internal Set of Artifacts
	 */
	public Set<Artifact> getArtifacts() {
		return new HashSet<Artifact>(this.artifacts);
	}

	/**
	 * Returns the intersection of the set of Artifacts in this Profile
	 * and that presented to this method if strict is true, otherwise
	 * return the intersection plus all artifacts in the set which have
	 * no match within the profile when only groupId and artifactId are
	 * taken into account.
	 * @return filtered list of Artifact objects
	 */
	public Set<Artifact> filter(Set<Artifact> s) {
		Set<Artifact> result = new HashSet<Artifact>();
		for (Artifact a : s) {
			if (artifacts.contains(a)) {
				// Exact match to an entry in the profile so include it
				result.add(a);
			}
			else if (strict == false) {
				if (containsOtherVersion(a) == false) {
					result.add(a);
				}
			}
		}
		return result;
	}
	
	/**
	 * @param a Artifact to look for ignoring version information
	 * @return whether there is a matching pair of groupId,artifactId in this profile
	 */
	private boolean containsOtherVersion(Artifact a) {
		for (Artifact a2 : artifacts) {
			if (a2.getArtifactId().equals(a.getArtifactId()) &&
					a2.getGroupId().equals(a.getGroupId())) {
				return true;
			}
		}
		return false;
	}
	
}
