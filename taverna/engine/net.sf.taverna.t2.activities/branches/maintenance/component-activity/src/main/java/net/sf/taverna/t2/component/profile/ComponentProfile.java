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

import static com.hp.hpl.jena.rdf.model.ModelFactory.createOntologyModel;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static net.sf.taverna.t2.component.profile.BaseProfileLocator.getBaseProfile;
import static net.sf.taverna.t2.workflowmodel.health.HealthCheck.NO_PROBLEM;
import static net.sf.taverna.t2.workflowmodel.health.RemoteHealthChecker.contactEndpoint;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.log4j.Logger.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
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
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import uk.org.taverna.ns._2012.component.profile.Activity;
import uk.org.taverna.ns._2012.component.profile.Ontology;
import uk.org.taverna.ns._2012.component.profile.Port;
import uk.org.taverna.ns._2012.component.profile.Profile;
import uk.org.taverna.ns._2012.component.profile.SemanticAnnotation;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;

/**
 * A ComponentProfile specifies the inputs, outputs and semantic annotations
 * that a Component must contain.
 * 
 * @author David Withers
 */
public class ComponentProfile implements
		net.sf.taverna.t2.component.api.Profile {
	private static final Logger logger = getLogger(ComponentProfile.class);
	private static final Map<String, OntModel> ontologyModels = new HashMap<String, OntModel>();
	private static final JAXBContext jaxbContext;
	static {
		try {
			jaxbContext = JAXBContext.newInstance(Profile.class);
		} catch (JAXBException e) {
			// Should never happen! Represents a critical error
			throw new Error(
					"failed to initialize profile deserialization engine", e);
		}
	}
	private static final net.sf.taverna.t2.component.api.Profile baseProfile = getBaseProfile();

	private net.sf.taverna.t2.component.api.Profile parent;
	private Profile profileDoc;
	private ExceptionHandling exceptionHandling;
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
			profileDoc = getProfile(new SAXSource(new InputSource(
					profileURL.toExternalForm())));
			parentRegistry = registry;
		} catch (JAXBException e) {
			throw new RegistryException("Unable to read profile", e);
		}
	}

	public ComponentProfile(Registry registry, String profileString)
			throws RegistryException {
		try {
			profileDoc = getProfile(new StreamSource(new StringReader(
					profileString)));
			this.parentRegistry = registry;
		} catch (JAXBException e) {
			throw new RegistryException("Unable to read profile", e);
		}
	}

	private static Profile getProfile(Source source) throws JAXBException {
		return jaxbContext.createUnmarshaller()
				.unmarshal(source, Profile.class).getValue();
	}

	@Override
	public Registry getComponentRegistry() {
		return parentRegistry;
	}

	@Override
	public String getXML() throws RegistryException {
		try {
			StringWriter stringWriter = new StringWriter();
			jaxbContext.createMarshaller().marshal(profileDoc, stringWriter);
			return stringWriter.toString();
		} catch (JAXBException e) {
			throw new RegistryException("Unable to serialize profile", e);
		}
	}

	@Override
	public Profile getProfileDocument() {
		return profileDoc;
	}

	@Override
	public String getId() {
		return profileDoc.getId();
	}

	@Override
	public String getName() {
		return profileDoc.getName();
	}

	@Override
	public String getDescription() {
		return profileDoc.getDescription();
	}

	/**
	 * @return Is this the base profile?
	 */
	private boolean isBase() {
		return baseProfile == null || baseProfile == this;
	}

	private synchronized net.sf.taverna.t2.component.api.Profile parent()
			throws RegistryException {
		if (parent == null) {
			if (!isBase() && profileDoc.getExtends() != null
					&& parentRegistry != null) {
				parent = parentRegistry.getComponentProfile(profileDoc
						.getExtends().getProfileId());
				if (parent != null)
					return parent;
			}
			parent = new EmptyProfile();
		}
		return parent;
	}

	@Override
	public String getOntologyLocation(String ontologyId) {
		String ontologyURI = null;
		for (Ontology ontology : profileDoc.getOntology())
			if (ontology.getId().equals(ontologyId))
				ontologyURI = ontology.getValue();
		if ((ontologyURI == null) && !isBase())
			ontologyURI = baseProfile.getOntologyLocation(ontologyId);
		return ontologyURI;
	}

	private Map<String, String> internalGetPrefixMap() throws RegistryException {
		Map<String, String> result = new TreeMap<String, String>();
		for (Ontology ontology : profileDoc.getOntology())
			result.put(ontology.getId(), ontology.getValue());
		result.putAll(parent().getPrefixMap());
		return result;
	}

	@Override
	public Map<String, String> getPrefixMap() throws RegistryException {
		Map<String, String> result = internalGetPrefixMap();
		if (!isBase())
			result.putAll(baseProfile.getPrefixMap());
		return result;
	}

	private OntModel readOntologyFromURI(String ontologyId, String ontologyURI) {
		OntModel model = createOntologyModel();
		InputStream in = null;
		try {
			URL url = new URL(ontologyURI);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// CRITICAL: must be retrieved as correct content type
			conn.addRequestProperty("Accept",
					"application/rdf+xml,application/xml;q=0.9");
			in = conn.getInputStream();	
			// TODO Consider whether the encoding is handled right
			// ontologyModel.read(in, url.toString());
			model.read(new StringReader(IOUtils.toString(in, "UTF-8")), url.toString());
		} catch (MalformedURLException e) {
			logger.error("Problem reading ontology " + ontologyId, e);
			return null;
		} catch (IOException e) {
			logger.error("Problem reading ontology " + ontologyId, e);
			return null;
		} catch (NullPointerException e) {
			// TODO Why is this different?
			logger.error("Problem reading ontology " + ontologyId, e);
			model = createOntologyModel();
		} finally {
			if (in != null)
				closeQuietly(in);
		}
		return model;
	}

	private boolean isAccessible(String ontologyURI) {
		return contactEndpoint(null, ontologyURI).getResultId() == NO_PROBLEM;
	}

	@Override
	public OntModel getOntology(String ontologyId) {
		String ontologyURI = getOntologyLocation(ontologyId);
		synchronized (ontologyModels) {
			if (ontologyModels.containsKey(ontologyURI))
				return ontologyModels.get(ontologyURI);
		}

		// Drop out of critical section while we do I/O
		if (!isAccessible(ontologyURI)) {
			logger.warn("catastrophic problem contacting ontology source");
			// Catastrophic problem?!
			synchronized(ontologyModels) {
				ontologyModels.put(ontologyURI, null);
			}
 			return null;
		}
		OntModel model = readOntologyFromURI(ontologyId, ontologyURI);

		synchronized (ontologyModels) {
			if (model != null && !ontologyModels.containsKey(ontologyURI)) {
				ontologyModels.put(ontologyURI, model);
			}
			return ontologyModels.get(ontologyURI);
		}
	}

	@Override
	public List<PortProfile> getInputPortProfiles() {
		List<PortProfile> portProfiles = new ArrayList<PortProfile>();
		for (Port port : profileDoc.getComponent().getInputPort())
			portProfiles.add(new PortProfile(this, port));
		if (!isBase())
			portProfiles.addAll(baseProfile.getInputPortProfiles());
		return portProfiles;
	}

	@Override
	public List<SemanticAnnotationProfile> getInputSemanticAnnotationProfiles()
			throws RegistryException {
		List<SemanticAnnotationProfile> saProfiles = new ArrayList<SemanticAnnotationProfile>();
		List<PortProfile> portProfiles = getInputPortProfiles();
		portProfiles.addAll(parent().getInputPortProfiles());
		for (PortProfile portProfile : portProfiles)
			saProfiles.addAll(portProfile.getSemanticAnnotations());
		if (!isBase())
			saProfiles.addAll(baseProfile.getInputSemanticAnnotationProfiles());
		return getUniqueSemanticAnnotationProfiles(saProfiles);
	}

	@Override
	public List<PortProfile> getOutputPortProfiles() {
		List<PortProfile> portProfiles = new ArrayList<PortProfile>();
		for (Port port : profileDoc.getComponent().getOutputPort())
			portProfiles.add(new PortProfile(this, port));
		if (!isBase())
			portProfiles.addAll(baseProfile.getOutputPortProfiles());
		return portProfiles;
	}

	@Override
	public List<SemanticAnnotationProfile> getOutputSemanticAnnotationProfiles()
			throws RegistryException {
		List<SemanticAnnotationProfile> saProfiles = new ArrayList<SemanticAnnotationProfile>();
		List<PortProfile> portProfiles = getOutputPortProfiles();
		portProfiles.addAll(parent().getOutputPortProfiles());
		for (PortProfile portProfile : portProfiles)
			saProfiles.addAll(portProfile.getSemanticAnnotations());
		if (!isBase())
			saProfiles
					.addAll(baseProfile.getOutputSemanticAnnotationProfiles());
		return getUniqueSemanticAnnotationProfiles(saProfiles);
	}

	@Override
	public List<ActivityProfile> getActivityProfiles() {
		List<ActivityProfile> activityProfiles = new ArrayList<ActivityProfile>();
		for (Activity activity : profileDoc.getComponent().getActivity())
			activityProfiles.add(new ActivityProfile(this, activity));
		return activityProfiles;
	}

	@Override
	public List<SemanticAnnotationProfile> getActivitySemanticAnnotationProfiles()
			throws RegistryException {
		List<SemanticAnnotationProfile> saProfiles = new ArrayList<SemanticAnnotationProfile>();
		List<ActivityProfile> activityProfiles = getActivityProfiles();
		activityProfiles.addAll(parent().getActivityProfiles());
		for (ActivityProfile activityProfile : activityProfiles)
			saProfiles.addAll(activityProfile.getSemanticAnnotations());
		if (!isBase())
			saProfiles.addAll(baseProfile
					.getActivitySemanticAnnotationProfiles());
		return getUniqueSemanticAnnotationProfiles(saProfiles);
	}

	@Override
	public List<SemanticAnnotationProfile> getSemanticAnnotationProfiles()
			throws RegistryException {
		List<SemanticAnnotationProfile> saProfiles = getComponentProfiles();
		saProfiles.addAll(parent().getSemanticAnnotationProfiles());
		if (!isBase())
			saProfiles.addAll(baseProfile.getSemanticAnnotationProfiles());
		return saProfiles;
	}

	private List<SemanticAnnotationProfile> getComponentProfiles() {
		List<SemanticAnnotationProfile> saProfiles = new ArrayList<SemanticAnnotationProfile>();
		for (SemanticAnnotation semanticAnnotation : profileDoc.getComponent()
				.getSemanticAnnotation())
			saProfiles.add(new SemanticAnnotationProfile(this,
					semanticAnnotation));
		return saProfiles;
	}

	private List<SemanticAnnotationProfile> getUniqueSemanticAnnotationProfiles(
			List<SemanticAnnotationProfile> semanticAnnotationProfiles) {
		List<SemanticAnnotationProfile> uniqueSemanticAnnotations = new ArrayList<SemanticAnnotationProfile>();
		Set<OntProperty> predicates = new HashSet<OntProperty>();
		for (SemanticAnnotationProfile semanticAnnotationProfile : semanticAnnotationProfiles) {
			OntProperty prop = semanticAnnotationProfile.getPredicate();
			if (prop != null && !predicates.contains(prop)) {
				predicates.add(prop);
 				uniqueSemanticAnnotations.add(semanticAnnotationProfile);
 			}
		}
		return uniqueSemanticAnnotations;
	}

	@Override
	public ExceptionHandling getExceptionHandling() {
		if (exceptionHandling == null)
			if (profileDoc.getComponent().getExceptionHandling() != null)
				exceptionHandling = new ExceptionHandling(profileDoc
						.getComponent().getExceptionHandling());
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
		if (getId() == null)
			return other.getId() == null;
		return getId().equals(other.getId());
	}

	public OntClass getClass(String className) {
		for (Ontology ontology : profileDoc.getOntology()) {
			OntModel ontModel = getOntology(ontology.getId());
			if (ontModel != null) {
				OntClass result = ontModel.getOntClass(className);
				if (result != null)
					return result;
			}
		}
		return null;
	}

	@Override
	public void delete() throws RegistryException {
		throw new RegistryException("deletion not supported");
	}
}

/**
 * A simple do-nothing implementation of a profile. Used when there's no other
 * option for what a <i>real</i> profile extends.
 * 
 * @author Donal Fellows
 */
final class EmptyProfile implements net.sf.taverna.t2.component.api.Profile {
	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public Registry getComponentRegistry() {
		return null;
	}

	@Override
	public String getXML() throws RegistryException {
		throw new RegistryException("no document");
	}

	@Override
	public Profile getProfileDocument() {
		return new Profile();
	}

	@Override
	public String getId() {
		return "";
	}

	@Override
	public String getOntologyLocation(String ontologyId) {
		return "";
	}

	@Override
	public Map<String, String> getPrefixMap() {
		return emptyMap();
	}

	@Override
	public OntModel getOntology(String ontologyId) {
		return null;
	}

	@Override
	public List<PortProfile> getInputPortProfiles() {
		return emptyList();
	}

	@Override
	public List<SemanticAnnotationProfile> getInputSemanticAnnotationProfiles() {
		return emptyList();
	}

	@Override
	public List<PortProfile> getOutputPortProfiles() {
		return emptyList();
	}

	@Override
	public List<SemanticAnnotationProfile> getOutputSemanticAnnotationProfiles() {
		return emptyList();
	}

	@Override
	public List<ActivityProfile> getActivityProfiles() {
		return emptyList();
	}

	@Override
	public List<SemanticAnnotationProfile> getActivitySemanticAnnotationProfiles() {
		return emptyList();
	}

	@Override
	public List<SemanticAnnotationProfile> getSemanticAnnotationProfiles() {
		return emptyList();
	}

	@Override
	public ExceptionHandling getExceptionHandling() {
		return null;
	}

	@Override
	public void delete() throws RegistryException {
		throw new RegistryException("deletion forbidden");
	}
}
