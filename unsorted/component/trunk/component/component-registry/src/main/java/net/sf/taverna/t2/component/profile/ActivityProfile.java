package net.sf.taverna.t2.component.profile;

import java.util.ArrayList;
import java.util.List;

import uk.org.taverna.ns._2012.component.profile.Activity;
import uk.org.taverna.ns._2012.component.profile.SemanticAnnotation;

public class ActivityProfile {

	private final ComponentProfile componentProfile;
	private final Activity activity;

	public ActivityProfile(ComponentProfile componentProfile, Activity activity) {
		this.componentProfile = componentProfile;
		this.activity = activity;
	}

	public List<SemanticAnnotationProfile> getSemanticAnnotations() {
		List<SemanticAnnotationProfile> semanticAnnotationProfiles = new ArrayList<SemanticAnnotationProfile>();
		List<SemanticAnnotation> semanticAnnotations = activity.getSemanticAnnotation();
		for (SemanticAnnotation semanticAnnotation : semanticAnnotations) {
			semanticAnnotationProfiles.add(new SemanticAnnotationProfile(componentProfile, semanticAnnotation));
		}
		return semanticAnnotationProfiles;
	}

}
