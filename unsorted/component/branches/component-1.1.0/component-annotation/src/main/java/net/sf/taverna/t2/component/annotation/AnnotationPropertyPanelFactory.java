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

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createTypedLiteral;
import static java.lang.Integer.MIN_VALUE;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.getObjectName;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * 
 * 
 * @author Alan Williams
 */
public class AnnotationPropertyPanelFactory extends PropertyPanelFactorySPI {
	@Override
	public JComponent getInputComponent(
			SemanticAnnotationProfile semanticAnnotationProfile,
			Statement statement) {
		return getDefaultInputComponent(semanticAnnotationProfile, statement);
	}

	@Override
	public RDFNode getNewTargetNode(Statement originalStatement,
			JComponent component) {
		String newText = ((JTextArea) component).getText();
		if ((originalStatement == null)
				|| !getObjectName(originalStatement).equals(newText))
			return createTypedLiteral(newText);
		return null;
	}

	@Override
	public int getRatingForSemanticAnnotation(
			SemanticAnnotationProfile semanticAnnotationProfile) {
		OntProperty property = semanticAnnotationProfile.getPredicate();
		if (property.isAnnotationProperty())
			return 100;
		return MIN_VALUE;
	}

	@Override
	public JComponent getDisplayComponent(
			SemanticAnnotationProfile semanticAnnotationProfile,
			Statement statement) {
		return getDefaultDisplayComponent(semanticAnnotationProfile, statement);
	}
}
