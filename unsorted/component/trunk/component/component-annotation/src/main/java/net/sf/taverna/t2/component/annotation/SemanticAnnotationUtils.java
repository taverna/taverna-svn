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

import java.util.Date;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.annotationbeans.SemanticAnnotation;

import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 *
 *
 * @author David Withers
 */
public class SemanticAnnotationUtils {

	public static String getDisplayName(RDFNode node) {
		if (node == null) {
			return "unknown";
		} else if (node.isAnon()) {
			return "anon";
		} else if (node.isLiteral()) {
			return node.asLiteral().getLexicalForm();
		} else if (node.isResource()) {
			Resource resource = node.asResource();
			if (resource instanceof OntResource) {
				String label = ((OntResource) resource).getLabel(null);
				if (label != null) {
					return label;
				}
			}
			String localName = resource.getLocalName();
			if ((localName != null) && !localName.isEmpty()) {
				return localName;
			}
			return resource.toString();

		} else {
			return "unknown";
		}
	}
	
	public static SemanticAnnotation findSemanticAnnotation(Annotated<?> annotated) {
		Date latestDate = null;
		SemanticAnnotation annotation = null;
		for (AnnotationChain chain : annotated.getAnnotations()) {
			for (AnnotationAssertion<?> assertion : chain.getAssertions()) {
				AnnotationBeanSPI detail = assertion.getDetail();
				if (detail instanceof SemanticAnnotation) {
					Date assertionDate = assertion.getCreationDate();
					if ((latestDate == null) || latestDate.before(assertionDate)) {
						annotation = (SemanticAnnotation) detail;
						latestDate = assertionDate;
					}
				}
			}
		}
		return annotation;
	}



}
