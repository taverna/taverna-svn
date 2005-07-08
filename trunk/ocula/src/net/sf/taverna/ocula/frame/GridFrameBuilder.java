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

package net.sf.taverna.ocula.frame;

import net.sf.taverna.ocula.ui.*;
import net.sf.taverna.ocula.Ocula;
import org.apache.log4j.Logger;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import org.jdom.Element;
import bsh.EvalError;

/**
 * Handles the &lt;grid&gt element and builds frames
 * which evaluate a script and lay out their components
 * within the specified number of columns. Produces
 * subclasses of the ResultSetPanel from the ui package.
 * @author Tom Oinn
 */
public class GridFrameBuilder implements FrameSPI {

    private static Logger log = Logger.getLogger(GridFrameBuilder.class);

    public String getElementName() {
	return "grid";
    }

    public OculaFrame makeFrame(Ocula o, Element element) {
	String name = element.getAttributeValue("name");
	if (name == null) {
	    name = "No Name";
	}
	String iconName = element.getAttributeValue("icon");
	if (iconName == null) {
	    iconName = "NoIcon";
	}
	Icon icon = Icons.getIcon(iconName);
	int cols = 3;
	try {
	    String colValue = element.getAttributeValue("cols","3");
	    cols = Integer.parseInt(colValue);
	}
	catch (NumberFormatException nfe) {
	    log.error("Tried to set columns to an invalid number, check the XML");
	}
	final GridFrame gf = new GridFrame(name, icon, cols);
	final Ocula ocula = o;
	Element scriptElement = element.getChild("script");
	final String script;
	if (scriptElement == null) {
	    // Will fail at eval time but that's fine, that's
	    // a checked exception and easier to deal with
	    script = "";
	}
	else {
	    script = scriptElement.getTextTrim();
	}
	new Thread() {
	    public void run() {
		gf.getProgressBar().setValue(0);
		gf.getProgressBar().setIndeterminate(true);
		try {
		    Object result = ocula.evaluate(script);
		    // Convert Collection to array
		    if (result instanceof Collection) {
			result = ((Collection)result).toArray();
		    }
		    // Handle Object[]
		    if (result instanceof Object[]) {
			Object[] array = (Object[])result;
			for (int i = 0; i < array.length; i++) {
			    gf.getContents().add(ocula.getRendererHandler().getRenderer(array[i]));
			    gf.revalidate();
			}
		    }
		}
		catch (EvalError ee) {
		    gf.getContents().add(new ErrorLabel("<html><body>Can't fetch components.<p>See log output for more details.</body></html>"));
		    gf.revalidate();
		}
		gf.getProgressBar().setValue(100);
		gf.getProgressBar().setIndeterminate(false);
	    }
	}.start();
	return gf;
    }
    
    /**
     * Quick inner class as the ResultSetPanel doesn't implement
     * the OculaFrame interface
     */
    class GridFrame extends ResultSetPanel implements OculaFrame {
	public GridFrame(String name, Icon icon, int cols) {
	    super(name, icon);
	    remove(contentsPanel);
	    contentsPanel = new GridPanel(cols);
	    contentsPanel.setBorder(BorderFactory.createLineBorder(ColourSet.getColour("ocula.panelbackground"),2));
	    contentsPanel.setBackground(Color.WHITE);
	    add(contentsPanel, BorderLayout.CENTER);
	}
    }
    
}
