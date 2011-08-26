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
 * Filename           $RCSfile: SeqhoundScavengerHelper.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-06-29 16:23:44 $
 *               by   $Author: sowen70 $
 * Created on 15-Jun-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.seqhound;

import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

public class SeqhoundScavengerHelper implements ScavengerHelper {

	private static Logger logger = Logger.getLogger(SeqhoundScavengerHelper.class);

	public Set<Scavenger> getDefaults() {
		Set<Scavenger> result = new HashSet<Scavenger>();
		try {
			result.add(new SeqhoundScavenger());
		} catch (ScavengerCreationException e) {
			logger.error("Error creating SeqhoundScavenger", e);
		}
		return result;
	}

	public Set<Scavenger> getFromModel(ScuflModel model) {
		return new HashSet<Scavenger>();
	}

	
	//TODO: a temporary hack to allow Seqhound to look like a ScavengerHelper and therefore
	//		be added to the service list, but by returning null prevents it being added as a
	//      menu item in ScavengerTreePopup
	public ActionListener getListener(ScavengerTree theScavenger) {		
		return null;
	}

	public String getScavengerDescription() { 
		return null;
	}
	
	/**
	 * Returns the icon for this scavenger
	 */
	public ImageIcon getIcon() {
		return new SeqhoundProcessorInfoBean().icon();
	}

	
	

}
