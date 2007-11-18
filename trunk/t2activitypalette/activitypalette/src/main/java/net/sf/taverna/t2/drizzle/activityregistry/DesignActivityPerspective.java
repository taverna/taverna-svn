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
 * Filename           $RCSfile: DesignActivityPerspective.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-11-18 23:43:10 $
 *               by   $Author: alaninmcr $
 * Created on 8 Nov 2006
 *****************************************************************/
package net.sf.taverna.t2.drizzle.activityregistry;

import java.io.InputStream;

import javax.swing.ImageIcon;

import net.sf.taverna.perspectives.AbstractPerspective;
import net.sf.taverna.perspectives.WorkflowPerspective;

import org.embl.ebi.escience.scuflui.TavernaIcons;

public class DesignActivityPerspective extends AbstractPerspective implements WorkflowPerspective {	

	@Override
	public ImageIcon getButtonIcon() {
		return TavernaIcons.editIcon;
	}	

	@Override
	public String getText() {
		return "Design with Activity Palette"; //$NON-NLS-1$
	}

	@Override
	protected InputStream getLayoutResourceStream() {
		return DesignActivityPerspective.class.getResourceAsStream("/perspective-design-activity.xml"); //$NON-NLS-1$
	}

	@Override
	public int positionHint() {
		return 10;
	}	
	
	
	
}
