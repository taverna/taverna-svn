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
/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeRootNode;

/**
 * @author alanrw
 * 
 * @param <O> The class of Object to which the leaf nodes correspond.
 *
 */
public final class PropertiedTreeRootNodeImpl<O> extends PropertiedTreeNodeImpl<O> implements
		PropertiedTreeRootNode<O> {

	/**
	 * 
	 */
	public PropertiedTreeRootNodeImpl() {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "root"; //$NON-NLS-1$
	}
}
