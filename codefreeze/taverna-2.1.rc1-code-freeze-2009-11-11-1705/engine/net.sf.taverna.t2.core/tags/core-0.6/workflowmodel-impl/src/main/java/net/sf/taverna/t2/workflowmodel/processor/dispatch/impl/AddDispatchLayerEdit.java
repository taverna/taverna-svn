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
package net.sf.taverna.t2.workflowmodel.processor.dispatch.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;

/**
 * Edit object to add a new DispatchLayer instance to a DispatchStackImpl
 * 
 * @author Tom Oinn
 * 
 */
public class AddDispatchLayerEdit extends AbstractDispatchLayerEdit {

	private DispatchLayer<?> layer;

	private int index;

	public AddDispatchLayerEdit(DispatchStack stack, DispatchLayer<?> newLayer,
			int index) {
		super(stack);
		this.layer = newLayer;
		this.index = index;
	}

	@Override
	protected void doEditAction(DispatchStackImpl stack) throws EditException {
		stack.addLayer(this.layer, this.index);
	}

	@Override
	protected void undoEditAction(DispatchStackImpl stack) {
		stack.removeLayer(layer);
	}

}
