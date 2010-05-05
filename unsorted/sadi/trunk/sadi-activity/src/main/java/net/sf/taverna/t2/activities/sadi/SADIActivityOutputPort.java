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

import net.sf.taverna.t2.workflowmodel.AbstractOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

/**
 * A {@link SADIActivity} output port.
 * 
 * @author David Withers
 */
public class SADIActivityOutputPort extends AbstractOutputPort implements ActivityOutputPort,
		SADIActivityPort {

	private final SADIActivity sadiActivity;
	private final RestrictionNode restriction;

	/**
	 * Constructs a new SADIActivityOutputPort.
	 * 
	 * @param sadiActivity
	 * @param restriction
	 * @param name
	 * @param portDepth
	 */
	public SADIActivityOutputPort(SADIActivity sadiActivity, RestrictionNode restriction, String name,
			int portDepth) {
		this(sadiActivity, restriction, name, portDepth, portDepth);
	}

	/**
	 * Constructs a new SADIActivityOutputPort.
	 * 
	 * @param sadiActivity
	 * @param restriction
	 * @param name
	 * @param portDepth
	 * @param granularDepth
	 */
	public SADIActivityOutputPort(SADIActivity sadiActivity, RestrictionNode restriction, String name,
			int portDepth, int granularDepth) {
		super(name, portDepth, granularDepth);
		this.sadiActivity = sadiActivity;
		this.restriction = restriction;
	}

	public SADIActivity getSADIActivity() {
		return sadiActivity;
	}

	public OntClass getOntClass() {
		return restriction.getOntClass();
	}

	public OntProperty getOntProperty() {
		return restriction.getOntProperty();
	}

	public List<?> getValues(String id) {
		return restriction.getValues(id);
	}

	public void setValues(String id, List<?> values) {
		restriction.setValues(id, values);
	}

	public void clearValues(String id) {
		restriction.clearValues(id);
	}

}
