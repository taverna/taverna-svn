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
 * Filename           $RCSfile: Icons.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-15 15:17:34 $
 *               by   $Author: sowen70 $
 * Created on 23 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class Icons {

	private static Map<String,ImageIcon> iconMap = new HashMap<String, ImageIcon>();
	
	static {
		ImageIcon icon = new ImageIcon(Icons.class.getResource(("/org/tango-project/tango-icon-theme/22x22/places/user-trash.png")));
		iconMap.put("delete",icon);
		
		icon = new ImageIcon(Icons.class.getResource(("/org/tango-project/tango-icon-theme/22x22/actions/document-properties.png")));
		iconMap.put("configure",icon);
		
		icon = new ImageIcon(Icons.class.getResource(("/org/tango-project/tango-icon-theme/16x16/actions/go-jump.png")));
		iconMap.put("selected", icon);
		
		icon = new ImageIcon(Icons.class.getResource(("/org/tango-project/tango-icon-theme/22x22/actions/document-open.png")));
		iconMap.put("view", icon);
		
		icon = new ImageIcon(Icons.class.getResource(("/org/tango-project/tango-icon-theme/22x22/categories/preferences-system.png")));
		iconMap.put("admin", icon);
		
		icon = new ImageIcon(Icons.class.getResource(("/org/tango-project/tango-icon-theme/22x22/actions/go-previous.png")));
		iconMap.put("back", icon);
		
		icon = new ImageIcon(Icons.class.getResource(("/org/tango-project/tango-icon-theme/22x22/actions/go-jump.png")));
		iconMap.put("toggle", icon);
		
		icon = new ImageIcon(Icons.class.getResource(("/org/tango-project/tango-icon-theme/22x22/devices/media-floppy.png")));
		iconMap.put("save", icon);
		
		icon = new ImageIcon(Icons.class.getResource(("/org/tango-project/tango-icon-theme/16x16/categories/applications-system.png")));
		iconMap.put("tree-op", icon);
		
		icon = new ImageIcon(Icons.class.getResource(("/org/tango-project/tango-icon-theme/22x22/actions/list-add.png")));
		iconMap.put("expand", icon);
		
		icon = new ImageIcon(Icons.class.getResource(("/org/tango-project/tango-icon-theme/22x22/actions/list-remove.png")));
		iconMap.put("collapse", icon);
		
		
		
		
	}
	
	public static ImageIcon getIcon(String name) {
		return iconMap.get(name);
	}
}
