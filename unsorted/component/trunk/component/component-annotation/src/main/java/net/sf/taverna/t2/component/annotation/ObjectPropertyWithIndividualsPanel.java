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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 *
 *
 * @author David Withers
 */
public class ObjectPropertyWithIndividualsPanel extends PropertyAnnotationPanel {

	public ObjectPropertyWithIndividualsPanel() {
		super();
	}
	
	JComboBox<NamedResource> resources;

	public ObjectPropertyWithIndividualsPanel(
			final SemanticAnnotationContextualView semanticAnnotationContextualView,
			final SemanticAnnotationProfile semanticAnnotationProfile) {
		setLayout(new BorderLayout());

		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(new EmptyBorder(5, 5, 0, 0));
		messagePanel.setBackground(Color.WHITE);
		add(messagePanel, BorderLayout.NORTH);

		JLabel inputLabel = new JLabel("Add Semantic Annotation");
		inputLabel.setBackground(Color.WHITE);
		Font baseFont = inputLabel.getFont();
		inputLabel.setFont(baseFont.deriveFont(Font.BOLD));
		messagePanel.add(inputLabel, BorderLayout.NORTH);

		JTextArea inputText = new JTextArea("Select a value for the annotation '"
				+ SemanticAnnotationUtils.getDisplayName(semanticAnnotationProfile.getPredicate())
				+ "'");
		inputText.setMargin(new Insets(5, 10, 10, 10));
		inputText.setMinimumSize(new Dimension(0, 30));
		inputText.setFont(baseFont.deriveFont(11f));
		inputText.setEditable(false);
		inputText.setFocusable(false);
		messagePanel.add(inputText, BorderLayout.CENTER);

		List<Individual> individuals = semanticAnnotationProfile.getIndividuals();
		NamedResource[] namedResources = new NamedResource[individuals.size()];
		for (int i = 0; i < namedResources.length; i++) {
			namedResources[i] = new NamedResource(individuals.get(i));
		}
		resources = new JComboBox<NamedResource>(namedResources);
		resources.setEditable(false);
		JPanel resourcePanel = new JPanel(new BorderLayout());
		resourcePanel.add(resources, BorderLayout.NORTH);
		resourcePanel.setBorder(new EmptyBorder(15, 5, 5, 5));
		add(resourcePanel, BorderLayout.CENTER);
	}

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
	RDFNode getNewTargetNode() {
		return ((NamedResource) resources.getSelectedItem()).getResource();
	}

}
