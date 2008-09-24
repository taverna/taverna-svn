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

import java.io.*;
import org.jdom.*;
import org.jdom.input.*;
import java.util.*;
import java.net.*;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.geom.Point2D;
import org.apache.log4j.Logger;

/**
 * Manages colours within an Ocula system
 * @author Tom Oinn
 */
public final class ColourSet {

    private static Logger log = Logger.getLogger(ColourSet.class); 
    private static Map colours;

    static {
	colours = new HashMap();
	URL defaultColourURL = Thread.currentThread().getContextClassLoader().getResource("net/sf/taverna/ocula/ui/defaultcolours.xml");
	log.info("Loading default colours from "+defaultColourURL);
	try {
	    InputStream is = defaultColourURL.openStream();
	    SAXBuilder builder = new SAXBuilder(false);
	    Document document = null;
	    document = builder.build(new InputStreamReader(is));
	    Element e = document.getRootElement();
	    loadFrom(e);
	}
	catch (Exception ex) {
	    log.error("Unable to load default colour map", ex);
	}
    }
    
    /**
     * Don't instantiate this class
     */
    private ColourSet() {
	//
    }

    /**
     * Import colours from the specified Element, this Element must contain
     * children with element name 'colour' of the form :
     * <pre>&lt;colour name='NAME' red='0-255' green='0-255' blue='0-255'&gt;</pre>
     */
    public static void loadFrom(Element e) {
	List l = e.getChildren("colour");
	for (Iterator i = l.iterator(); i.hasNext();) {
	    try {
		Element colourElement = (Element)i.next();
		String colourName = colourElement.getAttributeValue("name");
		Color colourValue = new Color(Integer.parseInt(colourElement.getAttributeValue("red")),
					      Integer.parseInt(colourElement.getAttributeValue("green")),
					      Integer.parseInt(colourElement.getAttributeValue("blue")));
		colours.put(colourName, colourValue);
		log.debug("Loaded colour '"+colourName+"'");
	    }
	    catch (Exception ex) {
		log.error("Failed to parse colour element", ex);
	    }
	}
    }
    
    /**
     * Return the named colour, or Color.WHITE if it can't be found
     */
    public static Color getColour(String name) {
	Color c = (Color)colours.get(name);
	if (c == null) {
	    c = Color.WHITE;
	}
	return c;
    }
    
    /**
     * Return the named colour faded half to white, or Color.WHITE if
     * the named colour can't be found
     */
    public static Color getShaded(String name) {
	Color colour = getColour(name);
	return new Color((colour.getRed() + 510) / 3,
			 (colour.getGreen() + 510) / 3,
			 (colour.getBlue() + 510) / 3);
    }

    /**
     * Get a gradient paint object for the specified colour name, this
     * will use the getShaded method to compute a paler form of the colour
     * and the two coordinate points to determine the gradient paint.
     */
    public static GradientPaint getShadePaint(String name, Point2D from, Point2D to) {
	return new GradientPaint(from, getColour(name), to, getShaded(name));
    }
    
    /**
     * Clear the current loaded colour set
     */
    public static void clearColours() {
	colours = new HashMap();
    }

}
