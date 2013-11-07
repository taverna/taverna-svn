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

import static java.awt.Color.WHITE;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.getObjectName;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * 
 * 
 * @author David Withers
 */
public abstract class PropertyPanelFactorySPI {
	public abstract JComponent getInputComponent(
			SemanticAnnotationProfile semanticAnnotationProfile,
			Statement statement);

	/**
	 * Returns null if the target node is the same as the original statement
	 * 
	 * @param origStatement
	 * @param inputComponent
	 * @return
	 */
	public abstract RDFNode getNewTargetNode(Statement origStatement,
			JComponent inputComponent);

	public abstract int getRatingForSemanticAnnotation(
			SemanticAnnotationProfile semanticAnnotationProfile);

	public abstract JComponent getDisplayComponent(
			SemanticAnnotationProfile semanticAnnotationProfile,
			Statement statement);

	public static JComponent getDefaultInputComponent(
			SemanticAnnotationProfile semanticAnnotationProfile,
			Statement statement) {
		JTextArea inputText = new JTextArea(20, 80);
		if (statement != null)
			inputText.setText(getObjectName(statement));
		inputText.setLineWrap(true);
		inputText.setWrapStyleWord(true);
		return inputText;
	}

	public static JComponent getDefaultDisplayComponent(
			SemanticAnnotationProfile semanticAnnotationProfile,
			Statement statement) {
		JTextArea value = new JTextArea(getObjectName(statement));
		value.setLineWrap(true);
		value.setWrapStyleWord(true);
		value.setEditable(false);
		value.setBackground(WHITE);
		value.setOpaque(true);
		value.setBorder(new EmptyBorder(2, 4, 2, 4));
		return value;
	}
}
