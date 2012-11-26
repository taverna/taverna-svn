package net.sf.taverna.t2.component.profile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.IllegalParameterException;

public class ComponentProfile {

	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
			.newInstance();
	private static Map<String, OntModel> ontologyModels = new HashMap<String, OntModel>();

	private Map<String, String> ontologyURIs = new HashMap<String, String>();
	private final URL profileURL;

	private Node profile;

	public ComponentProfile(String profileURL) {
		try {
			this.profileURL = new URL(profileURL);
		} catch (MalformedURLException e) {
			throw new IllegalParameterException("Invalid URL : " + profileURL);
		}
	}

	public ComponentProfile(URL profileURL) {
		this.profileURL = profileURL;
	}

	public Node getProfile() {
		if (profile == null) {
			try {
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				Document document = documentBuilder.parse(profileURL.openStream());
				profile = getFirstNode(document, "profile");
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return profile;
	}

	public String getName() {
		return getNodeText(getFirstNode(getProfile(), "name"));
	}

	public String getDescription() {
		return getNodeText(getFirstNode(getProfile(), "description"));
	}

	public String getOntologyLocation(String ontologyId) {
		List<Node> nodes = getNodes(getProfile(), "ontology");
		for (Node node : nodes) {
			Node ontology = node.getAttributes().getNamedItem("id");
			if (ontologyId.equals(ontology.getNodeValue())) {
				return getNodeText(node);
			}
		}
		return null;
	}

	public OntModel getOntology(String ontologyId) {
		if (!ontologyURIs.containsKey(ontologyId)) {
			ontologyURIs.put(ontologyId, getOntologyLocation(ontologyId));
		}
		String ontologyURI = ontologyURIs.get(ontologyId);
		if (!ontologyModels.containsKey(ontologyURI)) {
			OntModel ontologyModel = ModelFactory.createOntologyModel();
			ontologyModel.read(ontologyURI);
			ontologyModels.put(ontologyURI, ontologyModel);
		}
		return ontologyModels.get(ontologyURI);
	}

	public List<String> getOntologyIds() {
		List<String> ontologyIds = new ArrayList<String>();
		List<Node> nodes = getNodes(getProfile(), "ontology");
		for (Node node : nodes) {
			Node ontology = node.getAttributes().getNamedItem("id");
			ontologyIds.add(ontology.getNodeValue().trim());
		}
		return ontologyIds;
	}

	public List<PortProfile> getInputPortProfiles() {
		return getPortProfiles("inputPort");
	}

	public List<SemanticAnnotationProfile> getInputSemanticAnnotationProfiles() {
		List<Node> nodes = getNodes(getFirstNode(profile, "component"), "inputPort");
		return getSemanticAnnotationProfiles(nodes);
	}

	public List<PortProfile> getOutputPortProfiles() {
		return getPortProfiles("outputPort");
	}

	public List<SemanticAnnotationProfile> getOutputSemanticAnnotationProfiles() {
		List<Node> nodes = getNodes(getFirstNode(profile, "component"), "outputPort");
		return getSemanticAnnotationProfiles(nodes);
	}

	public Set<ActivityProfile> getActivityProfiles() {
		Set<ActivityProfile> activityProfiles = new HashSet<ActivityProfile>();
		NodeList ports = getProfile().getOwnerDocument().getElementsByTagName("activity");
		for (int i = 0; i < ports.getLength(); i++) {
			activityProfiles.add(new ActivityProfile(ports.item(i)));
		}
		return activityProfiles;
	}

	public List<SemanticAnnotationProfile> getActivitySemanticAnnotationProfiles() {
		List<Node> nodes = getNodes(getFirstNode(profile, "component"), "activity");
		return getSemanticAnnotationProfiles(nodes);
	}

	public List<SemanticAnnotationProfile> getSemanticAnnotationProfiles() {
		return getSemanticAnnotationProfiles(getFirstNode(profile, "component"));
	}

	private String getNodeText(Node node) {
		if (node == null) {
			return null;
		} else {
			return node.getTextContent().trim();
		}
	}

	public List<SemanticAnnotationProfile> getSemanticAnnotationProfiles(Node node) {
		List<SemanticAnnotationProfile> semanticAnnotations = new ArrayList<SemanticAnnotationProfile>();
		List<Node> nodes = getNodes(node, "semanticAnnotation");
		for (Node child : nodes) {
			semanticAnnotations.add(new SemanticAnnotationProfile(this, child));
		}
		return semanticAnnotations;
	}

	private List<SemanticAnnotationProfile> getSemanticAnnotationProfiles(List<Node> nodes) {
		List<SemanticAnnotationProfile> semanticAnnotations = new ArrayList<SemanticAnnotationProfile>();
		Set<OntProperty> predicates = new HashSet<OntProperty>();
		for (Node node : nodes) {
			for (SemanticAnnotationProfile semanticAnnotationProfile : getSemanticAnnotationProfiles(node)) {
				if (!predicates.contains(semanticAnnotationProfile.getPredicate())) {
					predicates.add(semanticAnnotationProfile.getPredicate());
					semanticAnnotations.add(semanticAnnotationProfile);
				}
			}
		}
		return semanticAnnotations;
	}

	public static Node getFirstNode(Node node, String nodeName) {
		if (node != null) {
			NodeList childNodes = node.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				if (nodeName.equals(childNodes.item(i).getNodeName())) {
					return childNodes.item(i);
				}
			}
		}
		return null;
	}

	public static List<Node> getNodes(Node node, String nodeName) {
		List<Node> nodes = new ArrayList<Node>();
		if (node != null) {
			NodeList childNodes = node.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				if (nodeName.equals(childNodes.item(i).getNodeName())) {
					nodes.add(childNodes.item(i));
				}
			}
		}
		return nodes;
	}

	private List<PortProfile> getPortProfiles(String portType) {
		List<PortProfile> portProfiles = new ArrayList<PortProfile>();
		NodeList ports = getProfile().getOwnerDocument().getElementsByTagName(portType);
		for (int i = 0; i < ports.getLength(); i++) {
			portProfiles.add(new PortProfile(this, ports.item(i)));
		}
		return portProfiles;
	}

	@Override
	public String toString() {
		return "ComponentProfile" + "\n  Name : " + getName() + "\n  Description : "
				+ getDescription() + "\n  InputPortProfiles : " + getInputPortProfiles()
				+ "\n  OutputPortProfiles : " + getOutputPortProfiles();
	}

}
