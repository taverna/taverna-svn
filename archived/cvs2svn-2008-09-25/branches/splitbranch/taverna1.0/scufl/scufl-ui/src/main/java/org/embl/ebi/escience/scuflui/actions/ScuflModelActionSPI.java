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
 * Filename           $RCSfile: ScuflModelActionSPI.java,v $
 * Revision           $Revision: 1.1.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-05 16:40:57 $
 *               by   $Author: davidwithers $
 * Created on 05-Jul-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui.actions;

import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * Abstract class through which optional ScuflModelActions may added using the SPI pattern.
 * This class is required to generate a default constructor and the ability to add a model after construction of the class.
 * It also enforces that the label for the action within the action itself.
 * 
 * These actions are added to the toolbar of the Advanced Model Explorer.
 * 
 * @author Stuart Owen
 * @see org.embl.ebi.escience.scuflui.actions.ScuflModelActionRegistry
 * @see org.embl.ebi.escience.scuflui.AdvancedModelExplorer
 */

public abstract class ScuflModelActionSPI extends ScuflModelAction {
	
	public ScuflModelActionSPI() {
		super(null);
	}
	
	public void setModel(ScuflModel model){
		this.model=model;
	}
	
	/**
	 * 
	 * @return the label that is asigned to the action when added to the Advanced Model Explorer
	 */
	public abstract String getLabel();
}
