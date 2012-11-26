package net.sf.taverna.t2.component.profile;

import java.util.List;

import org.w3c.dom.Node;

public class PortProfile {

	private final ComponentProfile componentProfile;
	private final Node portNode;

	public PortProfile(ComponentProfile componentProfile, Node portNode) {
		this.componentProfile = componentProfile;
		this.portNode = portNode;
	}

	public List<SemanticAnnotationProfile> getSemanticAnnotations() {
		return componentProfile.getSemanticAnnotationProfiles(portNode);
	}

	@Override
	public String toString() {
		return "PortProfile \n  SemanticAnnotations : " + getSemanticAnnotations();
	}

}
