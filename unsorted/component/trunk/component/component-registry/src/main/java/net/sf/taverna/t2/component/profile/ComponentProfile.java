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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.local.LocalComponent;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.RemoteHealthChecker;

import uk.org.taverna.ns._2012.component.profile.Activity;
import uk.org.taverna.ns._2012.component.profile.ExceptionHandling;
import uk.org.taverna.ns._2012.component.profile.Ontology;
import uk.org.taverna.ns._2012.component.profile.Port;
import uk.org.taverna.ns._2012.component.profile.Profile;
import uk.org.taverna.ns._2012.component.profile.SemanticAnnotation;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * A ComponentProfile specifies the inputs, outputs and semantic annotations
 * that a Component must contain.
 *
 * @author David Withers
 */
public class ComponentProfile {

	private static Logger logger = Logger.getLogger(ComponentProfile.class);

	private static Map<String, OntModel> ontologyModels = new HashMap<String, OntModel>();

	private JAXBContext jaxbContext;
	private Profile profile;

	public ComponentProfile(URL profileURL) throws ComponentRegistryException {
		try {
			jaxbContext = JAXBContext.newInstance(Profile.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			profile = (Profile) unmarshaller.unmarshal(profileURL);
		} catch (JAXBException e) {
			throw new ComponentRegistryException("Unable to read profile", e);
		}
	}

	public ComponentProfile(String profileString) throws ComponentRegistryException {
		try {
			jaxbContext = JAXBContext.newInstance(Profile.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			profile = (Profile) unmarshaller.unmarshal(new StreamSource( new StringReader(profileString)));
		} catch (JAXBException e) {
			throw new ComponentRegistryException("Unable to read profile", e);
		}
	}

	public String getXML() throws ComponentRegistryException {
		StringWriter stringWriter = new StringWriter();
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(profile, stringWriter);
		} catch (JAXBException e) {
			throw new ComponentRegistryException("Unable to read profile", e);
		}
		return stringWriter.toString();
	}

	public Profile getProfile() {
		return profile;
	}

	public String getId() {
		return profile.getId();
	}

	public String getName() {
		return profile.getName();
	}

	public String getDescription() {
		return profile.getDescription();
	}

	public String getOntologyLocation(String ontologyId) {
		String ontologyURI = null;
		List<Ontology> ontologies = profile.getOntology();
		for (Ontology ontology : ontologies) {
			if (ontology.getId().equals(ontologyId)) {
				ontologyURI = ontology.getValue();
			}
		}
		return ontologyURI;
	}

	public OntModel getOntology(String ontologyId) {
		String ontologyURI = getOntologyLocation(ontologyId);
		if (!ontologyModels.containsKey(ontologyURI)) {
			OntModel ontologyModel = ModelFactory.createOntologyModel();
			VisitReport report = RemoteHealthChecker.contactEndpoint(null, ontologyURI);
			if (report.getResultId() != HealthCheck.NO_PROBLEM) {
				return null;
			}
			InputStream in;
			String ontologyAsString;
			try {
				in = new URL(ontologyURI).openStream();
			ontologyAsString = IOUtils.toString(in);
			IOUtils.closeQuietly(in);
			} catch (MalformedURLException e) {
				logger.error(e);
				return null;
			} catch (IOException e) {
				logger.error(e);
				return null;
			}
			if (!ontologyAsString.startsWith("<?")) {
				return null;
			}
			ontologyModel.read(new StringReader(ontologyAsString), null);
			ontologyModels.put(ontologyURI, ontologyModel);
		}
		return ontologyModels.get(ontologyURI);
	}

	public List<PortProfile> getInputPortProfiles() {
		List<PortProfile> portProfiles = new ArrayList<PortProfile>();
		List<Port> ports = profile.getComponent().getInputPort();
		for (Port port : ports) {
			portProfiles.add(new PortProfile(this, port));
		}
		return portProfiles;
	}

	public List<SemanticAnnotationProfile> getInputSemanticAnnotationProfiles() {
		List<SemanticAnnotationProfile> semanticAnnotationsProfiles = new ArrayList<SemanticAnnotationProfile>();
		List<PortProfile> portProfiles = getInputPortProfiles();
		for (PortProfile portProfile : portProfiles) {
			semanticAnnotationsProfiles.addAll(portProfile.getSemanticAnnotations());
		}
		return getUniqueSemanticAnnotationProfiles(semanticAnnotationsProfiles);
	}

	public List<PortProfile> getOutputPortProfiles() {
		List<PortProfile> portProfiles = new ArrayList<PortProfile>();
		List<Port> ports = profile.getComponent().getOutputPort();
		for (Port port : ports) {
			portProfiles.add(new PortProfile(this, port));
		}
		return portProfiles;
	}

	public List<SemanticAnnotationProfile> getOutputSemanticAnnotationProfiles() {
		List<SemanticAnnotationProfile> semanticAnnotationsProfiles = new ArrayList<SemanticAnnotationProfile>();
		List<PortProfile> portProfiles = getOutputPortProfiles();
		for (PortProfile portProfile : portProfiles) {
			semanticAnnotationsProfiles.addAll(portProfile.getSemanticAnnotations());
		}
		return getUniqueSemanticAnnotationProfiles(semanticAnnotationsProfiles);
	}

	public List<ActivityProfile> getActivityProfiles() {
		List<ActivityProfile> activityProfiles = new ArrayList<ActivityProfile>();
		List<Activity> activities = profile.getComponent().getActivity();
		for (Activity activity : activities) {
			activityProfiles.add(new ActivityProfile(this, activity));
		}
		return activityProfiles;
	}

	public List<SemanticAnnotationProfile> getActivitySemanticAnnotationProfiles() {
		List<SemanticAnnotationProfile> semanticAnnotationsProfiles = new ArrayList<SemanticAnnotationProfile>();
		List<ActivityProfile> activityProfiles = getActivityProfiles();
		for (ActivityProfile activityProfile : activityProfiles) {
			semanticAnnotationsProfiles.addAll(activityProfile.getSemanticAnnotations());
		}
		return getUniqueSemanticAnnotationProfiles(semanticAnnotationsProfiles);
	}

	public List<SemanticAnnotationProfile> getSemanticAnnotationProfiles() {
		List<SemanticAnnotationProfile> semanticAnnotationsProfiles = new ArrayList<SemanticAnnotationProfile>();
		for (SemanticAnnotation semanticAnnotation : profile.getComponent().getSemanticAnnotation()) {
			semanticAnnotationsProfiles
					.add(new SemanticAnnotationProfile(this, semanticAnnotation));
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
	
	public ExceptionHandling getExceptionHandling() {
		return profile.getComponent().getExceptionHandling();
	}

	@Override
	public String toString() {
		return "ComponentProfile" + "\n  Name : " + getName() + "\n  Description : "
				+ getDescription() + "\n  InputPortProfiles : " + getInputPortProfiles()
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

}
