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
 * Filename           $RCSfile: LogBookPerspective.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008-04-02 16:24:39 $
 *               by   $Author: stain $
 * Created on 8 Nov 2006
 *****************************************************************/
package uk.org.mygrid.logbook.ui.perspective;

import java.io.InputStream;

import javax.swing.ImageIcon;

import net.sf.taverna.perspectives.AbstractPerspective;
import uk.org.mygrid.logbook.ui.LogBookIcons;

public class LogBookPerspective extends AbstractPerspective {	

	public ImageIcon getButtonIcon() {
		return LogBookIcons.logBookIcon;
	}	

	public String getText() {
		return "LogBook";
	}

	@Override
	protected InputStream getLayoutResourceStream() {
		return LogBookPerspective.class.getResourceAsStream("/perspective-logbook.xml");
	}	
	
}
