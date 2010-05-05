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

import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPort;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

/**
 * A {@link SADIActivity} port.
 * 
 * @author David Withers
 */
public interface SADIActivityPort extends ActivityPort {

	/**
	 * Returns the {@link SADIActivity} that this port is attached to.
	 * 
	 * @return the {@link SADIActivity} that this port is attached to
	 */
	public SADIActivity getSADIActivity();

	/**
	 * Returns the {@link OntClass} for the port.
	 * 
	 * @return the {@link OntClass} for the port
	 */
	public OntClass getOntClass();

	/**
	 * Returns the {@link OntProperty} for the port. Returns <code>null</code> is no property
	 * is associated with the port.
	 * 
	 * @return the {@link OntProperty} for the port
	 */
	public OntProperty getOntProperty();

	/**
	 * Returns the list of values for the id.
	 * 
	 * @param id
	 * @return the list of values for the id
	 */
	public List<?> getValues(String id);

	/**
	 * Sets the values for the id.
	 * 
	 * @param id
	 * @param values
	 *            the new values for the id
	 */
	public void setValues(String id, List<?> values);

	/**
	 * Clears the values for the id.
	 * 
	 * @param id
	 */
	public void clearValues(String id);

}
