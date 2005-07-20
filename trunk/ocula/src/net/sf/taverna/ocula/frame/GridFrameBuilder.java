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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

import net.sf.taverna.ocula.Ocula;
import net.sf.taverna.ocula.Parser;
import net.sf.taverna.ocula.ui.ErrorLabel;
import net.sf.taverna.ocula.ui.GridPanel;
import net.sf.taverna.ocula.ui.Icons;
import net.sf.taverna.ocula.ui.OculaMenu;
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
	final Element doubleClickElement = element.getChild("doubleclick");
	final Element contextMenuElement = element.getChild("contextmenu");
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
	new Thread() {
	    public void run() {
		gf.getProgressBar().setValue(0);
		gf.getProgressBar().setIndeterminate(true);
		try {
		    Object result = new Parser(ocula).parseScript(element);
		    if (result instanceof Object[]) {
			Object[] array = (Object[])result;
			for (int i = 0; i < array.length; i++) {
			    final Object targetObject = array[i];
			    final JComponent component = ocula.getRendererHandler().getRenderer(targetObject);
			    gf.getContents().add(component);
			    // If there's a doubleclick action defined then register the appropriate
			    // mouse listener...
			    component.addMouseListener(new MouseAdapter() {
				    public void mouseClicked(MouseEvent me) {
					if (me.isPopupTrigger()) {
					    popupMenu(me);
					    return;
					}
					if (doubleClickElement != null && 
					    me.getClickCount() == 2 && 
					    me.isPopupTrigger() == false) {
					    log.debug("Double click");
					    ocula.putContext("selectedObject",targetObject);
					    ocula.getActionRunner().runAction(doubleClickElement);
					    ocula.removeKey("selectedObject");
					}
				    }
				    public void mousePressed(MouseEvent me) {
					if (me.isPopupTrigger()) {
					    popupMenu(me);
					}
				    }
				    public void mouseReleased(MouseEvent me) {
					if (me.isPopupTrigger()) {
					    popupMenu(me);
					}
				    }
				    public void popupMenu(MouseEvent e) {
					try {
					    if (contextMenuElement != null) {
						String menuTitle = contextMenuElement.getAttributeValue("title","No Title");
						Icon menuIcon = Icons.getIcon(contextMenuElement.getAttributeValue("icon","NoIcon"));
						List actions = contextMenuElement.getChildren();
						OculaMenu menu = new OculaMenu(menuTitle, menuIcon);
						for (Iterator j = actions.iterator(); j.hasNext();) {
						    final Element actionElement = (Element)j.next();
						    String actionElementName = actionElement.getAttributeValue("name","No Name");
						    Icon actionElementIcon = Icons.getIcon(actionElement.getAttributeValue("icon","NoIcon"));
						    JMenuItem item = new JMenuItem(actionElementName, actionElementIcon);
						    item.addActionListener(new ActionListener() {
							    public void actionPerformed(ActionEvent ae) {
								ocula.putContext("selectedObject",targetObject);
								ocula.getActionRunner().runAction(actionElement);
								ocula.removeKey("selectedObject");
							    }
							});
						    menu.add(item);
						}
						menu.show(component, e.getX(), e.getY());
					    }
					}
					catch (Exception ex) {
					    log.error("Exception in mousepressed handler", ex);
					}
				    }
				});
			    gf.revalidate();
			}
		    }
		}
		catch (EvalError ee) {
		    gf.getContents().add(new ErrorLabel("<html><body>Can't fetch components.<p>See log output for more details.</body></html>"));
		    gf.revalidate();
		    log.error("Can't evaluate script", ee);
		}
		catch (Exception ex) {
		    log.error("Unexpected exception!", ex);
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
	    contentsPanel = new GridPanel(cols);
	    setContents(contentsPanel);
	    setUpContents();
	}
    }
    
}
