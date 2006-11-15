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
 * Filename           $RCSfile: AbstractPerspective.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-11-15 12:52:28 $
 *               by   $Author: sowen70 $
 * Created on 15 Nov 2006
 *****************************************************************/
package net.sf.taverna.perspectives;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.swing.ImageIcon;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * An abstract implementation of a perspective that handles the storing of the
 * layout XML if modified via the 'update' method, which once set causes this XML to be used rather than
 * the bundled resource.
 * Concrete subclass should provide getText, getButtonIcon, and getLayoutResourceStream.
 * @author Stuart Owen
 *
 */
public abstract class AbstractPerspective implements PerspectiveSPI {

	private Element layoutElement = null;	

	public InputStream getLayoutInputStream() {
		if (layoutElement == null) return getLayoutResourceStream();
		else {
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			String xml=outputter.outputString(layoutElement);
			return new ByteArrayInputStream(xml.getBytes());
		}
	}

	public void update(Element layoutElement) {
		this.layoutElement = layoutElement;
	}
	
	/**
	 * The text for the perspective
	 */
	public abstract String getText();
	
	/**
	 * The icon for the perspective
	 */
	public abstract ImageIcon getButtonIcon();
	
	/**
	 * 
	 * @return the resource stream for the perspective
	 */
	protected abstract InputStream getLayoutResourceStream();

}
