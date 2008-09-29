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
 * Filename           $RCSfile: WebScavengerHelper.java,v $
 * Revision           $Revision: 1.1.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-05 16:40:27 $
 *               by   $Author: davidwithers $
 * Created on 03-Jul-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui;

import java.util.Set;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;

/**
 * An interface to specifically identify the WebScavengerHelper.
 * This is need to distinguish it from the other scavenger helpers so that is placement
 * in the menu can be controlled, and because WebScavenger requires ScavengerTree in its constructor
 * meaning that it needs this information passing to getDefaults.
 * 
 * @author Stuart Owen
 *
 */

public interface WebScavengerHelper {
	
	/**
	 * The same as getDefaults in ScavengerHelper, except that ScavengerTree is required
	 * to construct the WebScavengers.
	 * 
	 * @param tree
	 * @return
	 */
	public Set<Scavenger> getDefaults(ScavengerTree tree);

}
