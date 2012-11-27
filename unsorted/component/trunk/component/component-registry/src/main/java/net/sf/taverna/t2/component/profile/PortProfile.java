package net.sf.taverna.t2.component.profile;

import java.util.ArrayList;
import java.util.List;

import uk.org.taverna.ns._2012.component.profile.Port;
import uk.org.taverna.ns._2012.component.profile.SemanticAnnotation;

public class PortProfile {

	private final ComponentProfile componentProfile;
	private final Port port;

	public PortProfile(ComponentProfile componentProfile, Port port) {
		this.componentProfile = componentProfile;
		this.port = port;
	}

	public List<SemanticAnnotationProfile> getSemanticAnnotations() {
		List<SemanticAnnotationProfile> semanticAnnotationProfiles = new ArrayList<SemanticAnnotationProfile>();
		List<SemanticAnnotation> semanticAnnotations = port.getSemanticAnnotation();
		for (SemanticAnnotation semanticAnnotation : semanticAnnotations) {
			semanticAnnotationProfiles.add(new SemanticAnnotationProfile(componentProfile, semanticAnnotation));
		}
		return semanticAnnotationProfiles;
	}

	@Override
	public String toString() {
		return "PortProfile \n  SemanticAnnotations : " + getSemanticAnnotations();
	}

}
