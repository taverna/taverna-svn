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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 *
 *
 * @author Alan Williams
 */
public class DatatypePropertyPanel extends PropertyAnnotationPanel {

	private JTextArea inputText = new JTextArea(20, 80);

	public DatatypePropertyPanel(
			final SemanticAnnotationContextualView semanticAnnotationContextualView,
			final SemanticAnnotationProfile semanticAnnotationProfile) {
		super();
		this.setLayout(new BorderLayout());
		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(new EmptyBorder(5, 5, 0, 0));
		messagePanel.setBackground(Color.WHITE);
		add(messagePanel, BorderLayout.NORTH);

		JLabel inputLabel = new JLabel("Enter a value for the annotation");
		inputLabel.setBackground(Color.WHITE);
		Font baseFont = inputLabel.getFont();
		inputLabel.setFont(baseFont.deriveFont(Font.BOLD));
		messagePanel.add(inputLabel, BorderLayout.NORTH);

		JTextArea messageText = new JTextArea("Enter a value for the annotation '"
				+ SemanticAnnotationUtils.getDisplayName(semanticAnnotationProfile.getPredicate())
				+ "'");
		messageText.setMargin(new Insets(5, 10, 10, 10));
		messageText.setMinimumSize(new Dimension(0, 30));
		messageText.setFont(baseFont.deriveFont(11f));
		messageText.setEditable(false);
		messageText.setFocusable(false);
		messagePanel.add(messageText, BorderLayout.CENTER);
		
		add(new JScrollPane(inputText), BorderLayout.CENTER);
	}

	@Override
	RDFNode getNewTargetNode() {
		return ResourceFactory.createTypedLiteral(inputText.getText());
	}

}
