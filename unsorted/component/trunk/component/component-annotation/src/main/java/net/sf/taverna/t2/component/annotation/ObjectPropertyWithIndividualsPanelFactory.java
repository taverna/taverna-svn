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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.component.localworld.LocalWorld;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.lang.ui.DeselectingButton;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 *
 *
 * @author David Withers, Alan Williams
 */
public class ObjectPropertyWithIndividualsPanelFactory extends PropertyPanelFactorySPI {
	
	private static LocalWorld localWorld = LocalWorld.getInstance();

	@Override
	public int getRatingForSemanticAnnotation(
			SemanticAnnotationProfile semanticAnnotationProfile) {
		OntProperty property = semanticAnnotationProfile.getPredicate();
		if (property.isObjectProperty()) {
//			if (!semanticAnnotationProfile.getIndividuals().isEmpty()) {
				return 100;
//			}
		}
		return Integer.MIN_VALUE;
	}


	@Override
	public JComponent getInputComponent(SemanticAnnotationProfile semanticAnnotationProfile, Statement statement) {
		return new ComboBoxWithAdd(semanticAnnotationProfile, statement);
	}


	@Override
	public RDFNode getNewTargetNode(JComponent component) {
		ComboBoxWithAdd panel = (ComboBoxWithAdd) component;
		return (RDFNode) panel.getSelectedItem();
	}
	
	private static class ComboBoxWithAdd extends JPanel {
		
		OntClass rangeClass = null;
		JComboBox resources;
		public ComboBoxWithAdd(final SemanticAnnotationProfile semanticAnnotationProfile, Statement statement) {
			super(new GridBagLayout());
			
			OntResource range = semanticAnnotationProfile.getPredicate().getRange();
			if (range.isClass()) {
				rangeClass = range.asClass();
			}
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			List<Individual> individuals = semanticAnnotationProfile.getIndividuals();
			if (rangeClass != null) {
				individuals.addAll(localWorld.getIndividualsOfClass(rangeClass));
			}
			
			Resource origResource = null;

			if (statement != null) {
				origResource = (Resource) statement.getObject();
			}
			resources = new JComboBox(individuals.toArray());
			resources.setRenderer(new NodeListCellRenderer());
			resources.setEditable(false);
			if (origResource != null) {
				resources.setSelectedItem(origResource);
			}
			this.add(resources, gbc);
			
			gbc.gridy++;

			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.add(new DeselectingButton("Add existing", new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					String answer = JOptionPane.showInputDialog("Please enter the URL for the resource");
					resources.addItem(localWorld.createIndividual(answer, rangeClass));
				}}));
			buttonPanel.add(new DeselectingButton("Add new", new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
				}}));
			gbc.anchor = GridBagConstraints.EAST;
			this.add(buttonPanel, gbc);
		}
		
		public RDFNode getSelectedItem() {
			return (RDFNode) resources.getSelectedItem();
		}
	}

}
