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

import static java.io.File.createTempFile;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.log4j.Logger.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import uk.org.taverna.ns._2012.component.profile.SemanticAnnotation;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;

/**
 * Definition of a semantic annotation for a component element.
 * 
 * @author David Withers
 */
public class SemanticAnnotationProfile {
	private static final Logger log = getLogger(SemanticAnnotationProfile.class);
	private final ComponentProfile componentProfile;
	private final SemanticAnnotation semanticAnnotation;

	public SemanticAnnotationProfile(ComponentProfile componentProfile,
			SemanticAnnotation semanticAnnotation) {
		this.componentProfile = componentProfile;
		this.semanticAnnotation = semanticAnnotation;
	}

	/**
	 * Returns the ontology that defines semantic annotation.
	 * 
	 * @return the ontology that defines semantic annotation
	 */
	public OntModel getOntology() {
		String ontology = semanticAnnotation.getOntology();
		if (ontology == null)
			return null;
		return componentProfile.getOntology(ontology);
	}

	/**
	 * Returns the predicate for the semantic annotation.
	 * 
	 * @return the predicate for the semantic annotation
	 */
	public OntProperty getPredicate() {
		OntModel ontology = getOntology();
		if (ontology == null) {
			return null;
		}
		String predicate = semanticAnnotation.getPredicate();
		if (predicate == null) {
			return null;
		}
		if (predicate.contains("foaf")) {
			StringWriter sw = new StringWriter();
			ontology.writeAll(sw, null, "RDF/XML");
			try {
				File f = createTempFile("foaf", null);
				writeStringToFile(f, sw.toString());
			} catch (IOException e) {
				log.info("failed to write foaf ontology to temporary file", e);
			}
		}

		return ontology.getOntProperty(predicate);
	}

	public String getPredicateString() {
		return semanticAnnotation.getPredicate();
	}

	public String getClassString() {
		return semanticAnnotation.getClazz();
	}

	/**
	 * Returns the individual that the semantic annotation must use.
	 * 
	 * May be null if no explicit individual is required.
	 * 
	 * @return the individual that the semantic annotation must use
	 */
	public Individual getIndividual() {
		String individual = semanticAnnotation.getValue();
		if (individual == null || individual.isEmpty())
			return null;
		return getOntology().getIndividual(individual);
	}

	/**
	 * Returns the individuals in the range of the predicate defined in the
	 * ontology.
	 * 
	 * @return the individuals in the range of the predicate defined in the
	 *         ontology
	 */
	public List<Individual> getIndividuals() {
		OntModel ontology = getOntology();
		OntProperty prop = getPredicate();
		if (ontology == null || prop == null) {
			return new ArrayList<Individual>();
		}
		OntResource range = prop.getRange();
		if (range == null) {
			return new ArrayList<Individual>();
		}
		return ontology.listIndividuals(range).toList();
	}

	public Integer getMinOccurs() {
		return semanticAnnotation.getMinOccurs().intValue();
	}

	public Integer getMaxOccurs() {
		try {
			return Integer.valueOf(semanticAnnotation.getMaxOccurs());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return "SemanticAnnotation " + "\n Predicate : " + getPredicate()
				+ "\n Individual : " + getIndividual() + "\n Individuals : "
				+ getIndividuals();
	}

	public OntClass getRangeClass() {
		String clazz = this.getClassString();
		if (clazz != null)
			return componentProfile.getClass(clazz);

		OntProperty prop = getPredicate();
		if (prop == null) {
			return null;
		}
		OntResource range = prop.getRange();
		if (range != null && range.isClass()) {
			return range.asClass();
		}
		return null;
	}
}
