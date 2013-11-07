package net.sf.taverna.t2.component.api;

import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.component.profile.ActivityProfile;
import net.sf.taverna.t2.component.profile.ExceptionHandling;
import net.sf.taverna.t2.component.profile.PortProfile;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;

import com.hp.hpl.jena.ontology.OntModel;

public interface Profile extends NamedItem {

	Registry getComponentRegistry();

	String getXML() throws RegistryException;

	uk.org.taverna.ns._2012.component.profile.Profile getProfileDocument();

	String getId();

	String getOntologyLocation(String ontologyId);

	Map<String, String> getPrefixMap() throws RegistryException;

	OntModel getOntology(String ontologyId);

	List<PortProfile> getInputPortProfiles();

	List<SemanticAnnotationProfile> getInputSemanticAnnotationProfiles()
			throws RegistryException;

	List<PortProfile> getOutputPortProfiles();

	List<SemanticAnnotationProfile> getOutputSemanticAnnotationProfiles()
			throws RegistryException;

	List<ActivityProfile> getActivityProfiles();

	List<SemanticAnnotationProfile> getActivitySemanticAnnotationProfiles()
			throws RegistryException;

	List<SemanticAnnotationProfile> getSemanticAnnotationProfiles()
			throws RegistryException;

	ExceptionHandling getExceptionHandling();

	void delete() throws RegistryException;
}
