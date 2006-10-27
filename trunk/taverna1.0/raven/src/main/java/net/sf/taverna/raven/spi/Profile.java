package net.sf.taverna.raven.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
	
	private String version;
	private String name;
	
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
		this.strict=strict;
		
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
		
		//determine the version if available		
		Node profileVersionAttribute=document.getDocumentElement().getAttributes().getNamedItem("version");
		if (profileVersionAttribute!=null) {
			version=profileVersionAttribute.getNodeValue();
		}
		else {
			System.out.println("Profile document contains no version.");
		}
		
//		determine the name if available		
		Node profileNameAttribute=document.getDocumentElement().getAttributes().getNamedItem("name");
		if (profileNameAttribute!=null) {
			name=profileNameAttribute.getNodeValue();
		}
		else {
			System.out.println("Profile document contains no name.");
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
	 * Returns the version string of the Profile, or 'NO VERSION' if a version is not defined
	 */
	public String getVersion() {
		if (version==null) return "NO VERSION";
		else return version;
	}
	
	/**
	 * Allows an artifact to be added to the profile at runtime
	 * @param artifact
	 */
	public void addArtifact(Artifact artifact) {
		artifacts.add(artifact);
	}
	
	/**
	 * returns the name of the profile, or null if no name is defined
	 */
	public String getName() {
		return name;
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
	
	/**
	 * Selects the highest version artifact defined in the registry that fits the 
	 * artifactId and groupId. Useful for allowing artifacts to be defined without version with
	 * the profile dictating the version to be used. 
	 * 
	 * Version numbers
     * are treated as being integers under lexicographic ordering
     * with the '.' character as separator. Presence of a token is
     * assumed to be an indication of a later version when compared
     * to absence, i.e. 1.3 &lt; 1.3.1. Where tokens are not
     * parsable as integer numbers String ordering is applied to both
 	 * tokens.
 	 * 
	 * @param groupId
	 * @param artifactId
	 * @return the Artifact or null if not found
	 */
	public Artifact discoverArtifact(String groupId, String artifactId) {
		Artifact result=null;
		List<Artifact> matches=new ArrayList<Artifact>();
		for (Artifact a: artifacts) {
			if (a.getArtifactId().equals(artifactId) && a.getGroupId().equals(groupId)) {
				matches.add(a);
			}
		}
		if (matches.size()>0) {
				Comparator<Artifact> comparator=new Comparator<Artifact>(){
	
				public int compare(Artifact o1, Artifact o2) {
					if (o1.getVersion().equals(o2.getVersion()));
					return lessThan(o1.getVersion(), o2.getVersion()) ? 1 : -2;
					
				}
				
				private boolean lessThan(String a, String b) {
					String[] va = a.split("\\.");
					String[] vb = b.split("\\.");
					for (int i = 0; i < va.length || i < vb.length ; i++) {
						if (i == va.length) {
							return true;
						}
						else if (i == vb.length) {
							return false;
						}
						String ca = va[i];
						String cb = vb[i];
						
						try {
							int ia = Integer.parseInt(ca);
							int ib = Integer.parseInt(cb);
							if (ia < ib) {
								return true;
							}
							else if (ia > ib) {
								return false;
							}
						}
						catch (NumberFormatException nfe) {
							if (ca.compareTo(cb) > 0) {
								return false;
							}
							else if (ca.compareTo(cb) < 0) {
								return true;
							}
						}
					}
					return false;
				}
				
			};
			Collections.sort(matches,comparator);
			result=matches.get(0);
		}
		return result;
	}
	
	public void write(OutputStream outputStream) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc=builder.newDocument();
		Element element=doc.createElement("profile");
		doc.appendChild(element);
		if (version!=null) element.setAttribute("version",version);
		if (name!=null) element.setAttribute("name",name);
		for (Artifact a : getArtifacts()) {
			String groupId=a.getGroupId();
			String artifactId=a.getArtifactId();
			String version=a.getVersion();
			
			Element artifactElement=doc.createElement("artifact");
			artifactElement.setAttribute("groupId", groupId);
			artifactElement.setAttribute("artifactId", artifactId);
			artifactElement.setAttribute("version", version);
			element.appendChild(artifactElement);
		}
		
		Transformer transformer=TransformerFactory.newInstance().newTransformer();
		DOMSource source=new DOMSource(doc);
		StreamResult dest=new StreamResult(outputStream);
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(source, dest);		
	}
}
