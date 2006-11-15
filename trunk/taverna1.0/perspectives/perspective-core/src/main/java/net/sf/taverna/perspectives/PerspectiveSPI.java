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
 * Filename           $RCSfile: PerspectiveSPI.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-11-15 12:44:53 $
 *               by   $Author: sowen70 $
 * Created on 8 Nov 2006
 *****************************************************************/
package net.sf.taverna.perspectives;

import java.io.InputStream;
import javax.swing.ImageIcon;

import org.jdom.Element;

/**
 * SPI representing UI perspectives
 * @author Stuart Owen
 *
 */

public interface PerspectiveSPI {
	
	/**
	 * 
	 * @return the input stream to the layout XML
	 */
	public InputStream getLayoutInputStream();
	
	/**
	 * 
	 * @return the icon image for the toolbar button
	 */
	public ImageIcon getButtonIcon();
	
	/**
	 * 
	 * @return the text for the perspective
	 */
	public String getText();
	
	/**
	 * Store internally any changes to the layout xml
	 */
	public void update(Element layoutElement);
		
	
	
	
}
