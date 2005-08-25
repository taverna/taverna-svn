/*
 * Copyright 2005 Tom Oinn, EMBL-EBI and University of Manchester
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

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import net.sf.taverna.ocula.Ocula;
import net.sf.taverna.ocula.Parser;
import net.sf.taverna.ocula.ui.ErrorLabel;
import net.sf.taverna.ocula.ui.GridPanel;
import net.sf.taverna.ocula.ui.Icons;
import net.sf.taverna.ocula.ui.ResultSetPanel;

import org.apache.log4j.Logger;
import org.jdom.Element;

import bsh.EvalError;

/**
 * Handles the &lt;grid&gt element and builds frames
 * which evaluate a script and lay out their components
 * within the specified number of columns. Produces
 * subclasses of the ResultSetPanel from the ui package.
 * @author Tom Oinn
 * @author Ismael Juma
 */
public class GridFrameBuilder implements FrameSPI {

    private static Logger log = Logger.getLogger(GridFrameBuilder.class);
 
    public String getElementName() {
	return "grid";
    }

    public OculaFrame makeFrame(Ocula o, final Element element) {
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
	
	String key = element.getAttributeValue("key");
	if (key != null) {
	    o.putContext(key, new FrameAndElement(gf, element));
	}
	final Ocula ocula = o;
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		gf.getProgressBar().setValue(0);
		gf.getProgressBar().setIndeterminate(true);
	    }
	});
	final Runnable stopProgressBar = new Runnable() {
	    public void run() {
		gf.getProgressBar().setValue(100);
		gf.getProgressBar().setIndeterminate(false);
	    }
	};
	new Thread() {
	    public void run() {
		Parser parser = new Parser(ocula);
		try {
		    Object result = parser.parseScript(element);
		    if (result instanceof Object[]) {
			Object[] array = (Object[])result;
			for (int i = 0; i < array.length; i++) {
			    final Object targetObject = array[i];
			    final JComponent component = ocula.getRendererHandler().getRenderer(targetObject);
			    gf.getContents().add(component);
			    // If there's a doubleclick action defined then register the appropriate
			    // mouse listener...
			    parser.parseDoubleClick(element, component, targetObject);
			    parser.parseContextMenu(element, component, targetObject);
			    gf.revalidate();
			}
		    }
		}
		catch (EvalError ee) {
		    gf.getContents().add(new ErrorLabel("<html><body>Can't" +
		    		" fetch components.<p>See log output for more" +
		    		" details.</body></html>"));
		    gf.revalidate();
		    log.error("Can't evaluate script", ee);
		}
		catch (Exception ex) {
		    log.error("Unexpected exception!", ex);
		}
		SwingUtilities.invokeLater(stopProgressBar);
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
	    contentsPanel = new GridPanel(cols);
	    setContents(contentsPanel);
	    setUpContents();
	}
    }
    
}
