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
package net.sf.taverna.t2.component.annotation;

import static com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel;
import static java.lang.Long.MIN_VALUE;
import static org.apache.log4j.Logger.getLogger;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.annotationbeans.SemanticAnnotation;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * 
 * 
 * @author David Withers
 */
public class SemanticAnnotationUtils {
	protected static final String ENCODING = "TURTLE";
	/* Pretend-base for making relative URIs */
	private static String BASE = "widget://4aa8c93c-3212-487c-a505-3e337adf54a3/";
	private static Logger logger = getLogger(SemanticAnnotationUtils.class);

	public static String getObjectName(Statement statement) {
		return getDisplayName(statement.getObject());
	}

	public static String getDisplayName(RDFNode node) {
		if (node == null)
			return "unknown";
		else if (node.isAnon())
			return "anon";
		else if (node.isLiteral())
			return node.asLiteral().getLexicalForm();
		else if (node.isResource()) {
			Resource resource = node.asResource();
			if (resource instanceof OntResource) {
				String label = ((OntResource) resource).getLabel(null);
				if (label != null)
					return label;
			}
			String localName = resource.getLocalName();
			if ((localName != null) && !localName.isEmpty())
				return localName;
			return resource.toString();
		} else
			return "unknown";
	}

	public static SemanticAnnotation findSemanticAnnotation(
			Annotated<?> annotated) {
		Date latestDate = new Date(MIN_VALUE);
		SemanticAnnotation annotation = null;
		// Need to scan all assertions...
		for (AnnotationChain chain : annotated.getAnnotations())
			for (AnnotationAssertion<?> assertion : chain.getAssertions())
				if (assertion.getDetail() instanceof SemanticAnnotation
						&& latestDate.before(assertion.getCreationDate())) {
					annotation = (SemanticAnnotation) assertion.getDetail();
					latestDate = assertion.getCreationDate();
				}
		return annotation;
	}

	public static SemanticAnnotation createSemanticAnnotation(Model model) {
		SemanticAnnotation semanticAnnotation = new SemanticAnnotation();
		String turtle = createTurtle(model);
		semanticAnnotation.setContent(turtle);
		return semanticAnnotation;
	}

	/**
	 * @param model
	 * @return
	 */
	public static String createTurtle(Model model) {
		StringWriter stringWriter = new StringWriter();
		model.write(stringWriter, ENCODING, BASE);
		// Workaround for https://issues.apache.org/jira/browse/JENA-132
		String turtle = stringWriter.toString().replace(
				"widget://4aa8c93c-3212-487c-a505-3e337adf54a3/", "");
		return turtle;
	}

	public static Model populateModel(Annotated<?> annotated) {
		Model result = createDefaultModel();
		SemanticAnnotation annotation = findSemanticAnnotation(annotated);
		try {
			if (annotation != null && !annotation.getContent().isEmpty())
				populateModelFromString(result, annotation.getContent());
		} catch (Exception e) {
			logger.error("failed to construct semantic annotation model", e);
		}
		return result;
	}

	public static void populateModelFromString(Model result, String content) {
		result.read(new StringReader(content), BASE, ENCODING);
	}

	public static Resource createBaseResource(Model model) {
		return model.createResource(BASE);
	}

	/**
	 * Check if a profile is satisfied by a component.
	 * 
	 * @param dataflow
	 *            The component definition.
	 * @param componentProfile
	 *            The profile definition.
	 * @return The set of failed constraints. If empty, the profile is satisfied
	 *         by the component.
	 */
	public static Set<SemanticAnnotationProfile> checkComponent(
			Dataflow dataflow, Profile componentProfile) {
		// TODO Check port presence by name
		Set<SemanticAnnotationProfile> problemProfiles = new HashSet<SemanticAnnotationProfile>();
		Model model = populateModel(dataflow);
		Set<Statement> statements = model.listStatements().toSet();
		try {
			for (SemanticAnnotationProfile saProfile : componentProfile
					.getSemanticAnnotationProfiles()) {
				OntProperty predicate = saProfile.getPredicate();
				if (predicate == null)
					continue;
				int count = 0;
				for (Statement statement : statements)
					if (statement.getPredicate().equals(predicate))
						count++;
				if (count < saProfile.getMinOccurs())
					problemProfiles.add(saProfile);
				if (saProfile.getMaxOccurs() != null
						&& count > saProfile.getMaxOccurs())
					// The UI should prevent this, but check anyway
					problemProfiles.add(saProfile);
			}
		} catch (RegistryException e) {
			logger.error("failed to look up profiles for semantic annotations", e);
		}
		return problemProfiles;
	}
}
