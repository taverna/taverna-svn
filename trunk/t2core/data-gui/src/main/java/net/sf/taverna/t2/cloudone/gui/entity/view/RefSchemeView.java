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
package net.sf.taverna.t2.cloudone.gui.entity.view;

import javax.swing.JComponent;

/**
 * Extend this by sub views and implement the {@link #setEdit(boolean)}
 * appropriate to that implementation
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 * 
 */
public abstract class RefSchemeView extends JComponent {
	/**
	 * How the view should handle changes of state in its components eg.
	 * disable/enable buttons
	 * 
	 * @param editable
	 *            True if the view is to be editable, false if it is no longer
	 *            to be editable.
	 * @throws IllegalStateException
	 *             If the current fields were illegal for the model. The view
	 *             will remain editable.
	 */
	public abstract void setEdit(boolean editable) throws IllegalStateException;

}
