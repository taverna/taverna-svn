/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
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
package net.sf.taverna.t2.activities.sadi;

import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

import com.hp.hpl.jena.ontology.OntClass;

/**
 * A {@link SADIActivity} input port.
 * 
 * @author David Withers
 */
public class SADIActivityInputPort extends AbstractPort implements ActivityInputPort,
		SADIActivityPort {

	private final SADIActivity sadiActivity;
	private final OntClass ontClass;

	/**
	 * Constructs a new SADIActivityInputPort.
	 * 
	 * @param sadiActivity
	 * @param ontClass
	 * @param depth
	 */
	protected SADIActivityInputPort(SADIActivity sadiActivity, OntClass ontClass, String name,
			int depth) {
		super(name, depth);
		this.sadiActivity = sadiActivity;
		this.ontClass = ontClass;
	}

	public SADIActivity getSADIActivity() {
		return sadiActivity;
	}

	public OntClass getOntClass() {
		return ontClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort#
	 * allowsLiteralValues()
	 */
	public boolean allowsLiteralValues() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort#
	 * getHandledReferenceSchemes()
	 */
	public List<Class<? extends ExternalReferenceSPI>> getHandledReferenceSchemes() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort#
	 * getTranslatedElementClass()
	 */
	public Class<?> getTranslatedElementClass() {
		return null;
	}

}
