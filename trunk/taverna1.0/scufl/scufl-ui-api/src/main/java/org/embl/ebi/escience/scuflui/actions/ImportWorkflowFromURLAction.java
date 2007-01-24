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
 * Filename           $RCSfile: ImportWorkflowFromURLAction.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-01-24 12:09:33 $
 *               by   $Author: sowen70 $
 * Created on 24 Jan 2007
 *****************************************************************/
package org.embl.ebi.escience.scuflui.actions;

import java.awt.Component;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ModelMap;

@SuppressWarnings("serial")
public class ImportWorkflowFromURLAction extends OpenWorkflowFromURLAction{

	public ImportWorkflowFromURLAction(Component parentComponent) {
		super(parentComponent);		
	}
	
	protected void initialise() {
		putValue(SMALL_ICON, TavernaIcons.importUrlIcon);
		putValue(NAME, "Import workflow location ...");
		putValue(SHORT_DESCRIPTION, "Import a workflow from the web into the current workflow");
	}

	@Override
	protected ScuflModel getModel() {
		return (ScuflModel)ModelMap.getInstance().getNamedModel(ModelMap.CURRENT_WORKFLOW);
	}
	
	

	
	
}
