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

import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDdateTime;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createTypedLiteral;
import static java.lang.Integer.MIN_VALUE;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.text.DefaultCaret;

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * 
 * 
 * @author Alan Williams
 */
public class DateTimePropertyPanelFactory extends PropertyPanelFactorySPI {

	private static String DateTimeString = XSDdateTime.getURI();

	public DateTimePropertyPanelFactory() {
		super();
	}

	@Override
	public JComponent getInputComponent(
			SemanticAnnotationProfile semanticAnnotationProfile,
			Statement statement) {
		Date now = new Date();
		SpinnerDateModel dateModel = new SpinnerDateModel(now, null, now,
				Calendar.DAY_OF_MONTH);
		JSpinner s = new JSpinner(dateModel);
		JSpinner.DateEditor de = new JSpinner.DateEditor(s,
				"yyyy-MM-dd-HH-mm-ss");

		/*
		 * Suggested hack from
		 * http://www.coderanch.com/t/345684/GUI/java/JSpinner-DateEditor-Set-default-focus
		 */

		de.getTextField().setCaret(new DefaultCaret() {
			private static final long serialVersionUID = 6779256780590610172L;
			private boolean diverted = false;

			@Override
			public void setDot(int dot) {
				diverted = (dot == 0);
				if (diverted)
					dot = getComponent().getDocument().getLength();
				super.setDot(dot);
			}

			@Override
			public void moveDot(int dot) {
				if (diverted) {
					super.setDot(0);
					diverted = false;
				}
				super.moveDot(dot);
			}
		});
		s.setEditor(de);
		if (statement != null) {
			Object o = statement.getObject().asLiteral().getValue();
			if (o instanceof XSDDateTime)
				dateModel.setValue(((XSDDateTime) o).asCalendar().getTime());
		}
		return s;
	}

	@Override
	public RDFNode getNewTargetNode(Statement originalStatement,
			JComponent component) {
		JSpinner spinner = (JSpinner) component;
		Date d = (Date) spinner.getValue();
		if ((originalStatement == null)
				|| !originalStatement.getObject().asLiteral().getValue()
						.equals(d)) {
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(d);
			return createTypedLiteral(cal);
		}
		return null;
	}

	@Override
	public int getRatingForSemanticAnnotation(
			SemanticAnnotationProfile semanticAnnotationProfile) {
		OntProperty property = semanticAnnotationProfile.getPredicate();
		if ((property != null) && property.isDatatypeProperty()
				&& DateTimeString.equals(semanticAnnotationProfile
						.getClassString()))
			return 200;
		return MIN_VALUE;
	}

	@Override
	public JComponent getDisplayComponent(
			SemanticAnnotationProfile semanticAnnotationProfile,
			Statement statement) {
		JComponent result = getInputComponent(semanticAnnotationProfile,
				statement);
		result.setEnabled(false);
		return result;
	}
}
