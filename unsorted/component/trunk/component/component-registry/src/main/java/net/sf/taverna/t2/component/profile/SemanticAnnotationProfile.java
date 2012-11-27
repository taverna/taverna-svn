package net.sf.taverna.t2.component.profile;

import java.util.ArrayList;
import java.util.List;

import uk.org.taverna.ns._2012.component.profile.SemanticAnnotation;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;

public class SemanticAnnotationProfile {

	private final ComponentProfile componentProfile;
	private final SemanticAnnotation semanticAnnotation;

	public SemanticAnnotationProfile(ComponentProfile componentProfile,
			SemanticAnnotation semanticAnnotation) {
		this.componentProfile = componentProfile;
		this.semanticAnnotation = semanticAnnotation;
	}

	public OntModel getOntology() {
		String ontology = semanticAnnotation.getOntology();
		if (ontology != null) {
			return componentProfile.getOntology(ontology);
		} else {
			return null;
		}
	}

	public OntProperty getPredicate() {
		OntModel ontology = getOntology();
		String predicate = semanticAnnotation.getPredicate();
		if (ontology != null && predicate != null) {
			return ontology.getOntProperty(predicate);
		} else {
			return null;
		}
	}

	public OntClass getOntClass() {
		OntModel ontology = getOntology();
		String ontClass = semanticAnnotation.getClazz();
		if (ontology != null && ontClass != null) {
			return getOntology().getOntClass(ontClass);
		} else {
			return null;
		}
	}

	public Individual getIndividual() {
		String individual = semanticAnnotation.getValue();
		if (individual.isEmpty()) {
			return null;
		} else {
			return getOntology().getIndividual(individual);
		}
	}

	public List<Individual> getIndividuals() {
		OntModel ontology = getOntology();
		OntClass ontClass = getOntClass();
		if (ontology != null && ontClass != null) {
			return ontology.listIndividuals(ontClass).toList();
		} else {
			return new ArrayList<Individual>();
		}
	}

	@Override
	public String toString() {
		return "SemanticAnnotation " + "\n Predicate : " + getPredicate() + "\n Individual : "
				+ getIndividual() + "\n Individuals : " + getIndividuals();
	}

}
