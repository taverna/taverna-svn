/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.InputPort;

/**
 * Specialization of InputPort to capture the extra information required by
 * Activity instances.
 * 
 * @author Tom Oinn
 * 
 */
public interface ActivityInputPort extends InputPort {

	/**
	 * Declares that the DataDocument instances fed as input data (either
	 * directly or as elements of a collection) to this input port must contain
	 * at least one of the specified ReferenceScheme types. This is used to
	 * specify that e.g. an activity can only accept URLs, values or similar.
	 * 
	 * @return Class objects representing the reference scheme types which this
	 *         input can handle
	 */
	public List<Class<? extends ExternalReferenceSPI>> getHandledReferenceSchemes();

}
