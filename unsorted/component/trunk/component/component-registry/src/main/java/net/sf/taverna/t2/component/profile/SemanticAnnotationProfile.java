package net.sf.taverna.t2.component.profile;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;

public class SemanticAnnotationProfile {

	private final ComponentProfile componentProfile;
	private final Node semanticAnnotationNode;

	public SemanticAnnotationProfile(ComponentProfile componentProfile, Node semanticAnnotationNode) {
		this.componentProfile = componentProfile;
		this.semanticAnnotationNode = semanticAnnotationNode;
	}

	public OntModel getOntology() {
		Node ontology = semanticAnnotationNode.getAttributes().getNamedItem("ontology");
		if (ontology != null) {
			return componentProfile.getOntology(ontology.getNodeValue());
		} else {
			return null;
		}
	}

	public OntProperty getPredicate() {
		OntModel ontology = getOntology();
		Node predicate = semanticAnnotationNode.getAttributes().getNamedItem("predicate");
		if (ontology != null && predicate != null) {
			return ontology.getOntProperty(predicate.getNodeValue());
		} else {
			return null;
		}
	}

	public OntClass getOntClass() {
		OntModel ontology = getOntology();
		Node ontClass = semanticAnnotationNode.getAttributes().getNamedItem("class");
		if (ontology != null && ontClass != null) {
			return getOntology().getOntClass(ontClass.getNodeValue());
		} else {
			return null;
		}
	}

	public Individual getIndividual() {
		String individual = semanticAnnotationNode.getTextContent().trim();
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
		return "SemanticAnnotation "
				+ "\n Predicate : " + getPredicate()
				+ "\n Individual : " + getIndividual()
				+ "\n Individuals : " + getIndividuals();
	}

}
