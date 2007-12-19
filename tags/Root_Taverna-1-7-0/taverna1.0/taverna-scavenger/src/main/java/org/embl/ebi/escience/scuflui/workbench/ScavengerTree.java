/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: ScavengerTree.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-10-03 12:10:53 $
 *               by   $Author: sowen70 $
 * Created on 03-Jul-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui.workbench;

import java.awt.Frame;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetListener;

import javax.swing.tree.TreeModel;

/**
 * A tree showing available processors from some set of external
 * libraries or searches. Nodes corresponding to a single potential processor
 * instance should contain a user object implementing ProcessorFactory.
 * 
 * @author David Withers
 */
public interface ScavengerTree extends DragSourceListener,
		DragGestureListener, DropTargetListener {

	public abstract Frame getContainingFrame();
	
	/**
	 * Notifies the start of scavenging.
	 * 
	 * @param message the message to display
	 */
	public abstract void scavengingStarting(String message);
	
	/**
	 * Notifies the end of scavenging.
	 * 
	 */
	public abstract void scavengingDone();
	
	public abstract void setPopulating(boolean populating);

	public abstract boolean isPopulating();

	/**
     * Get the next available count and increment the counter
     */
	public abstract int getNextCount();

	public abstract TreeModel getModel();

	/**
     * Examine the model, create any scavengers that would have been required to
     * populate the model with its existing processors. Now handles all three
     * processor types.
     */
	public abstract void addScavengersFromModel()
			throws ScavengerCreationException;

	/**
     * Add a new scavenger to the tree, firing appropriate model events as we
     * do.
     */
	public abstract void addScavenger(Scavenger theScavenger);

}
