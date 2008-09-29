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
 * Filename           $RCSfile: XMLSplittableOutputPort.java,v $
 * Revision           $Revision: 1.1.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-05 16:40:37 $
 *               by   $Author: davidwithers $
 * Created on 04-Jul-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.wsdl;

import java.util.List;

import javax.swing.JMenuItem;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ScuflContextMenuAware;

/**
 * Specialised OutputPort that is able to generate ScuflContextMenu JMenu items
 * to allow the user to add XMLSplitters to that port
 * 
 * @author Stuart Owen
 *
 */

public class XMLSplittableOutputPort extends OutputPort implements ScuflContextMenuAware{

	public XMLSplittableOutputPort(Processor processor, String name) throws DuplicatePortNameException, PortCreationException {
		super(processor, name);		
	}	
	
	/**
	 * Generates the menu items for the port if applicable, otherwise returns an empty List
	 */
	public List<JMenuItem> contextMenuItems() {
		return XMLSplitterScuflContextMenuFactory.instance().contextItemsForPort(this);
	}
}
