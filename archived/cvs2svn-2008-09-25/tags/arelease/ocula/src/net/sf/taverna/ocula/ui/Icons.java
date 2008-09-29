/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.ocula.ui;

import javax.swing.ImageIcon;
import java.util.*;
import java.net.URL;

/**
 * Handles fetching of icons within the Ocula UI
 * @author Tom Oinn
 */
public abstract class Icons {

    private static Map iconCache = new HashMap();
    private static String[] suffixes = new String[]{"png","gif","jpeg","jpg"};
    private static ImageIcon unknownIcon;
    
    /**
     * Set up the 'unknown icon' image
     */
    static {
	URL url = Thread.currentThread().getContextClassLoader().
	    getResource("net/sf/taverna/ocula/ui/icons/unknownicon.png");
	unknownIcon = new ImageIcon(url);
    }
    
    /**
     * Don't instantiate this class
     */
    private Icons() {
	//
    }

    /**
     * Get the icon with the specified name, assumes that all icons
     * are .png, .gif or .jpeg (in that order) and located in the 
     * 'icons' subpackage of this package.
     * If the name doesn't match an 'unknown icon' icon is returned.
     */
    public static ImageIcon getIcon(String name) {
	ImageIcon icon = (ImageIcon)iconCache.get(name);
	if (icon != null) {
	    return icon;
	}
	for (int i = 0; i < suffixes.length; i++) {
	    URL resourceURL = Thread.currentThread().getContextClassLoader().
		getResource("net/sf/taverna/ocula/ui/icons/"+name+".png");
	    if (resourceURL != null) {
		icon = new ImageIcon(resourceURL);
		iconCache.put(name, icon);
		return icon;
	    }
	}
	return unknownIcon;
    }

}
