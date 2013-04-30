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

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 *
 *
 * @author David Withers
 */
public class ObjectPropertyWithIndividualsPanelFactory extends PropertyPanelFactorySPI {

	private class NamedResource {
	
		private final OntResource resource;
	
		public NamedResource(OntResource resource) {
			this.resource = resource;
		}
	
		public OntResource getResource() {
			return resource;
		}
	
		public String toString() {
			String label = resource.getLabel(null);
			if (label != null) {
				return label;
			}
				String localName = resource.getLocalName();
				if ((localName != null) && !localName.isEmpty()) {
					return localName;
				}
			return resource.toString();
		}
	
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((resource == null) ? 0 : resource.hashCode());
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
			NamedResource other = (NamedResource) obj;
			if (resource == null) {
				if (other.resource != null)
					return false;
			} else if (!resource.equals(other.resource))
				return false;
			return true;
		}
	
	}


	@Override
	public int getRatingForSemanticAnnotation(
			SemanticAnnotationProfile semanticAnnotationProfile) {
		OntProperty property = semanticAnnotationProfile.getPredicate();
		if (property.isObjectProperty()) {
			if (!semanticAnnotationProfile.getIndividuals().isEmpty()) {
				return 100;
			}
		}
		return Integer.MIN_VALUE;
	}


	@Override
	public JComponent getInputComponent(SemanticAnnotationProfile semanticAnnotationProfile, Statement statement) {
		JComboBox resources;
		List<Individual> individuals = semanticAnnotationProfile.getIndividuals();
		NamedResource[] namedResources = new NamedResource[individuals.size()];
		
		Resource origResource = null;
		NamedResource origNamedResource = null;
		if (statement != null) {
			origResource = (Resource) statement.getObject();
		}
		for (int i = 0; i < namedResources.length; i++) {
			Individual resource = individuals.get(i);
			namedResources[i] = new NamedResource(resource);
			if (resource.equals(origResource)) {
				origNamedResource = namedResources[i];
			}
			
		}
		resources = new JComboBox(namedResources);
		resources.setEditable(false);
		if (origNamedResource != null) {
			resources.setSelectedItem(origNamedResource);
		}
		
		return resources;
	}


	@Override
	public RDFNode getNewTargetNode(JComponent component) {
		JComboBox resources = (JComboBox) component;
		return ((NamedResource) resources.getSelectedItem()).getResource();
	}

}
