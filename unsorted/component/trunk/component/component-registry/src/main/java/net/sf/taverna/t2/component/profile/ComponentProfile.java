/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.component.profile;

import static net.sf.taverna.t2.component.profile.BaseProfileLocator.getBaseProfile;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.RemoteHealthChecker;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import uk.org.taverna.ns._2012.component.profile.Activity;
import uk.org.taverna.ns._2012.component.profile.Extends;
import uk.org.taverna.ns._2012.component.profile.Ontology;
import uk.org.taverna.ns._2012.component.profile.Port;
import uk.org.taverna.ns._2012.component.profile.Profile;
import uk.org.taverna.ns._2012.component.profile.SemanticAnnotation;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * A ComponentProfile specifies the inputs, outputs and semantic annotations
 * that a Component must contain.
 * 
 * @author David Withers
 */
public class ComponentProfile implements
		net.sf.taverna.t2.component.api.Profile {

	private static Logger logger = Logger.getLogger(ComponentProfile.class);

	private static Map<String, OntModel> ontologyModels = new HashMap<String, OntModel>();

	private static JAXBContext jaxbContext;
	static {
		try {
			jaxbContext = JAXBContext.newInstance(Profile.class);
		} catch (JAXBException e) {
			// Should never happen! Represents a critical error
			throw new Error(
					"failed to initialize profile deserialization engine", e);
		}
	}
	private Profile profile;
	private ExceptionHandling exceptionHandling;

	private static net.sf.taverna.t2.component.api.Profile baseProfile = getBaseProfile();

	private Registry parentRegistry = null;

	public ComponentProfile(URL profileURL) throws RegistryException {
		this(null, profileURL);
	}

	public ComponentProfile(String profileString) throws RegistryException {
		this(null, profileString);
	}

	public ComponentProfile(Registry registry, URI profileURI)
			throws RegistryException, MalformedURLException {
		this(registry, profileURI.toURL());
	}

	public ComponentProfile(Registry registry, URL profileURL)
			throws RegistryException {
		try {
			profile = (Profile) jaxbContext.createUnmarshaller().unmarshal(
					profileURL);
			parentRegistry = registry;
		} catch (JAXBException e) {
			throw new RegistryException("Unable to read profile", e);
		}
	}

	public ComponentProfile(Registry registry, String profileString)
			throws RegistryException {
		try {
			profile = (Profile) jaxbContext.createUnmarshaller().unmarshal(
					new StreamSource(new StringReader(profileString)));
			this.parentRegistry = registry;
		} catch (JAXBException e) {
			throw new RegistryException("Unable to read profile", e);
		}
	}

	@Override
	public Registry getComponentRegistry() {
		return parentRegistry;
	}

	@Override
	public String getXML() throws RegistryException {
		try {
			StringWriter stringWriter = new StringWriter();
			jaxbContext.createMarshaller().marshal(profile, stringWriter);
			return stringWriter.toString();
		} catch (JAXBException e) {
			throw new RegistryException("Unable to read profile", e);
		}
	}

	@Override
	public Profile getProfileDocument() {
		return profile;
	}

	@Override
	public String getId() {
		return profile.getId();
	}

	@Override
	public String getName() {
		return profile.getName();
	}

	@Override
	public String getDescription() {
		return profile.getDescription();
	}

	@Override
	public net.sf.taverna.t2.component.api.Profile getExtends()
			throws RegistryException {
		net.sf.taverna.t2.component.api.Profile result = null;
		Extends extends_ = profile.getExtends();
		if (extends_ != null) {
			for (net.sf.taverna.t2.component.api.Profile p : parentRegistry
					.getComponentProfiles()) {
				if (p.getId().equals(extends_.getProfileId())) {
					result = p;
					break;
				}
			}
		}
		return result;
	}

	@Override
	public String getOntologyLocation(String ontologyId) {
		String ontologyURI = null;
		List<Ontology> ontologies = profile.getOntology();
		for (Ontology ontology : ontologies) {
			if (ontology.getId().equals(ontologyId)) {
				ontologyURI = ontology.getValue();
			}
		}
		if ((ontologyURI == null)
				&& ((baseProfile != null) && (baseProfile != this))) {
			ontologyURI = baseProfile.getOntologyLocation(ontologyId);
		}
		return ontologyURI;
	}

	private TreeMap<String, String> internalGetPrefixMap()
			throws RegistryException {
		TreeMap<String, String> result = new TreeMap<String, String>();
		List<Ontology> ontologies = profile.getOntology();
		for (Ontology ontology : ontologies) {
			result.put(ontology.getId(), ontology.getValue());
		}
		net.sf.taverna.t2.component.api.Profile extends_ = getExtends();
		if (extends_ != null) {
			result.putAll(extends_.getPrefixMap());
		}

		return result;
	}

	@Override
	public TreeMap<String, String> getPrefixMap() throws RegistryException {
		TreeMap<String, String> result = internalGetPrefixMap();
		if (baseProfile != null && baseProfile != this) {
			result.putAll(baseProfile.getPrefixMap());
		}
		return result;
	}

	private OntModel readOntologyFromURI(String ontologyId, String ontologyURI)
			throws MalformedURLException, IOException {
		InputStream in = null;
		try {
			OntModel ontologyModel = ModelFactory.createOntologyModel();
			in = new URL(ontologyURI).openStream();
			ontologyModel.read(new StringReader(IOUtils.toString(in)), null);
			return ontologyModel;
		} finally {
			if (in != null)
				IOUtils.closeQuietly(in);
		}
	}

	@Override
	public OntModel getOntology(String ontologyId) {
		String ontologyURI = getOntologyLocation(ontologyId);
		if (!ontologyModels.containsKey(ontologyURI)) {
			VisitReport report = RemoteHealthChecker.contactEndpoint(null,
					ontologyURI);
			if (report.getResultId() != HealthCheck.NO_PROBLEM) {
				return null;
			}
			try {
				ontologyModels.put(ontologyURI,
						readOntologyFromURI(ontologyId, ontologyURI));
			} catch (MalformedURLException e) {
				logger.error("Problem reading ontology " + ontologyId, e);
				return null;
			} catch (IOException e) {
				logger.error("Problem reading ontology " + ontologyId, e);
				return null;
			} catch (NullPointerException e) {
				logger.error("Problem reading ontology " + ontologyId, e);
				// TODO Why is this different?
				ontologyModels.put(ontologyURI,
						ModelFactory.createOntologyModel());
			}
		}
		return ontologyModels.get(ontologyURI);
	}

	@Override
	public List<PortProfile> getInputPortProfiles() {
		List<PortProfile> portProfiles = new ArrayList<PortProfile>();
		List<Port> ports = profile.getComponent().getInputPort();
		for (Port port : ports) {
			portProfiles.add(new PortProfile(this, port));
		}
		if ((baseProfile != null) && (baseProfile != this)) {
			portProfiles.addAll(baseProfile.getInputPortProfiles());
		}
		return portProfiles;
	}

	@Override
	public List<SemanticAnnotationProfile> getInputSemanticAnnotationProfiles()
			throws RegistryException {
		List<SemanticAnnotationProfile> semanticAnnotationsProfiles = new ArrayList<SemanticAnnotationProfile>();
		List<PortProfile> portProfiles = getInputPortProfiles();
		net.sf.taverna.t2.component.api.Profile extends_ = getExtends();
		if (extends_ != null) {
			portProfiles.addAll(extends_.getInputPortProfiles());
		}
		for (PortProfile portProfile : portProfiles) {
			semanticAnnotationsProfiles.addAll(portProfile
					.getSemanticAnnotations());
		}
		if ((baseProfile != null) && (baseProfile != this)) {
			semanticAnnotationsProfiles.addAll(baseProfile
					.getInputSemanticAnnotationProfiles());
		}
		return getUniqueSemanticAnnotationProfiles(semanticAnnotationsProfiles);
	}

	@Override
	public List<PortProfile> getOutputPortProfiles() {
		List<PortProfile> portProfiles = new ArrayList<PortProfile>();
		List<Port> ports = profile.getComponent().getOutputPort();
		for (Port port : ports) {
			portProfiles.add(new PortProfile(this, port));
		}
		if ((baseProfile != null) && (baseProfile != this)) {
			portProfiles.addAll(baseProfile.getOutputPortProfiles());
		}
		return portProfiles;
	}

	@Override
	public List<SemanticAnnotationProfile> getOutputSemanticAnnotationProfiles()
			throws RegistryException {
		List<SemanticAnnotationProfile> semanticAnnotationsProfiles = new ArrayList<SemanticAnnotationProfile>();
		List<PortProfile> portProfiles = getOutputPortProfiles();
		net.sf.taverna.t2.component.api.Profile extends_ = getExtends();
		if (extends_ != null) {
			portProfiles.addAll(extends_.getOutputPortProfiles());
		}
		for (PortProfile portProfile : portProfiles) {
			semanticAnnotationsProfiles.addAll(portProfile
					.getSemanticAnnotations());
		}
		if ((baseProfile != null) && (baseProfile != this)) {
			semanticAnnotationsProfiles.addAll(baseProfile
					.getOutputSemanticAnnotationProfiles());
		}
		return getUniqueSemanticAnnotationProfiles(semanticAnnotationsProfiles);
	}

	@Override
	public List<ActivityProfile> getActivityProfiles() {
		List<ActivityProfile> activityProfiles = new ArrayList<ActivityProfile>();
		List<Activity> activities = profile.getComponent().getActivity();
		for (Activity activity : activities) {
			activityProfiles.add(new ActivityProfile(this, activity));
		}
		return activityProfiles;
	}

	@Override
	public List<SemanticAnnotationProfile> getActivitySemanticAnnotationProfiles()
			throws RegistryException {
		List<SemanticAnnotationProfile> semanticAnnotationsProfiles = new ArrayList<SemanticAnnotationProfile>();
		List<ActivityProfile> activityProfiles = getActivityProfiles();
		net.sf.taverna.t2.component.api.Profile extends_ = getExtends();
		if (extends_ != null) {
			activityProfiles.addAll(extends_.getActivityProfiles());
		}
		for (ActivityProfile activityProfile : activityProfiles) {
			semanticAnnotationsProfiles.addAll(activityProfile
					.getSemanticAnnotations());
		}
		if ((baseProfile != null) && (baseProfile != this)) {
			semanticAnnotationsProfiles.addAll(baseProfile
					.getActivitySemanticAnnotationProfiles());
		}
		return getUniqueSemanticAnnotationProfiles(semanticAnnotationsProfiles);
	}

	@Override
	public List<SemanticAnnotationProfile> getSemanticAnnotationProfiles()
			throws RegistryException {
		List<SemanticAnnotationProfile> semanticAnnotationsProfiles = getComponentProfiles();
		net.sf.taverna.t2.component.api.Profile extends_ = getExtends();
		if (extends_ != null) {
			semanticAnnotationsProfiles.addAll(extends_
					.getSemanticAnnotationProfiles());
		}
		if ((baseProfile != null) && (baseProfile != this)) {
			semanticAnnotationsProfiles.addAll(baseProfile
					.getSemanticAnnotationProfiles());
		}
		return semanticAnnotationsProfiles;
	}

	private List<SemanticAnnotationProfile> getComponentProfiles() {
		List<SemanticAnnotationProfile> semanticAnnotationsProfiles = new ArrayList<SemanticAnnotationProfile>();

		for (SemanticAnnotation semanticAnnotation : profile.getComponent()
				.getSemanticAnnotation()) {
			semanticAnnotationsProfiles.add(new SemanticAnnotationProfile(this,
					semanticAnnotation));
		}
		return semanticAnnotationsProfiles;
	}

	private List<SemanticAnnotationProfile> getUniqueSemanticAnnotationProfiles(
			List<SemanticAnnotationProfile> semanticAnnotationProfiles) {
		List<SemanticAnnotationProfile> uniqueSemanticAnnotations = new ArrayList<SemanticAnnotationProfile>();
		Set<OntProperty> predicates = new HashSet<OntProperty>();
		for (SemanticAnnotationProfile semanticAnnotationProfile : semanticAnnotationProfiles) {
			if (!predicates.contains(semanticAnnotationProfile.getPredicate())) {
				predicates.add(semanticAnnotationProfile.getPredicate());
				uniqueSemanticAnnotations.add(semanticAnnotationProfile);
			}
		}
		return uniqueSemanticAnnotations;
	}

	@Override
	public ExceptionHandling getExceptionHandling() {
		if (exceptionHandling == null) {
			uk.org.taverna.ns._2012.component.profile.ExceptionHandling proxied = profile
					.getComponent().getExceptionHandling();
			if (proxied != null) {
				exceptionHandling = new ExceptionHandling(proxied);
			}
		}
		return exceptionHandling;
	}

	@Override
	public String toString() {
		return "ComponentProfile" + "\n  Name : " + getName()
				+ "\n  Description : " + getDescription()
				+ "\n  InputPortProfiles : " + getInputPortProfiles()
				+ "\n  OutputPortProfiles : " + getOutputPortProfiles();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComponentProfile other = (ComponentProfile) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	public OntClass getClass(String className) {
		OntClass result = null;
		List<Ontology> ontologies = profile.getOntology();
		for (Ontology ontology : ontologies) {
			String id = ontology.getId();
			OntModel ontModel = this.getOntology(id);
			if (ontModel != null) {
				result = ontModel.getOntClass(className);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}

}
