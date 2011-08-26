/*
 * Copyright 2005 University of Manchester
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

package net.sf.taverna.ocula;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

import net.sf.taverna.ocula.ui.Icons;
import net.sf.taverna.ocula.ui.OculaMenu;

import org.apache.log4j.Logger;
import org.jdom.Element;

import bsh.EvalError;

/**
 * This class contains methods that parse elements that may appear in many
 * of the FrameBuilder classes. It helps maintain the code in a central place
 * and avoid duplication.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public class Parser {
    private Logger log = Logger.getLogger(Parser.class);

    private Ocula ocula;

    public Parser(Ocula ocula) {
	this.ocula = ocula;
    }

    /**
     * Looks for the first "script" tag, executes it, and returns the result
     * as an array.
     * @param element An Element containing a "script" tag.
     * @return An array of objects that contain the result of the script.
     * @throws EvalError
     */
    public Object[] parseScript(Element element) throws EvalError {
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

	Object result = ocula.evaluate(script);
	
	// Return an empty array if the result is null
	if (result == null) {
	    return new Object[0];
	}
	// Convert Collection to array
	if (result instanceof Collection) {
	    result = ((Collection) result).toArray();
	}
	// Handle Object[]
	if (result instanceof Object[] == false) {
	    log.debug("Got " + result.toString() + " from call");
	    Object[] resultArray = new Object[1];
	    resultArray[0] = result;
	    result = resultArray;
	}
	return (Object[]) result;
    }

    /**
     * Parses the &lt;doubleclick&gt; tag and it adds the appropriate mouse
     * listeners to the component received. When the listener is invoked,
     * it adds targetObject to the context and runs the action in the
     * doubleclick element. 
     * 
     * @param element Element containing a doubleclick element.
     * @param component JComponent where the mouse listeners should be added.
     * @param targetObject Object that should be added to the context before
     * running the action.
     */
    public void parseDoubleClick(Element element, JComponent component,
	    final Object targetObject) {
	final Element doubleClickElement = element.getChild("doubleclick");
	if (doubleClickElement == null) {
	    return;
	}
	component.addMouseListener(new MouseAdapter() {
	    public void mouseClicked(MouseEvent me) {
		if (me.getClickCount() == 2 && me.isPopupTrigger() == false) {
		    log.debug("Double click");
		    if (targetObject != null) {
			ocula.putContext("selectedObject", targetObject);
		    }
		    ocula.putContext("event", me);
		    ocula.getActionRunner().runAction(doubleClickElement);
		    if (targetObject != null) {
			ocula.removeKey("selectedObject");
		    }
		    ocula.removeKey("event");
		}
	    }
	});
    }

    /**
     * Parses the &lt;click&gt; tag and it adds the appropriate mouse
     * listeners to the component received. When the listener is invoked,
     * it adds targetObject to the context and runs the action in the
     * click element. 
     * 
     * @param element Element containing a click element.
     * @param component JComponent where the mouse listeners should be added.
     * @param targetObject Object that should be added to the context before
     * running the action.
     */
    public void parseClick(Element element, final JComponent component,
	    final Object targetObject) {
	final Element clickElement = element.getChild("click");
	if (clickElement == null) {
	    log.debug("No <click> element");
	    return;
	}
	log.debug("Adding a mouse listener.");
	component.addMouseListener(new MouseAdapter() {
	    public void mouseClicked(MouseEvent me) {
		if (me.isPopupTrigger() == false) {
		    log.debug("Click");
		    if (targetObject != null) {
			ocula.putContext("selectedObject", targetObject);
		    }
		    ocula.putContext("event", me);
		    ocula.getActionRunner().runAction(clickElement);
		    
		    if (targetObject != null) {
			ocula.removeKey("selectedObject");
		    }
		    ocula.removeKey("event");
		}
	    }
	});
    }

    /**
     * Parses the &lt;contextmenu&gt; tag and it adds the appropriate mouse
     * listeners to the component received. When the listener is invoked,
     * it adds targetObject to the context and it launches a popup menu
     * with the actions specified.
     * 
     * @param element Element containing a doubleclick element.
     * @param component JComponent where the mouse listeners should be added.
     * @param targetObject Object that should be added to the context before
     * running the action.
     */
    public void parseContextMenu(Element element, final JComponent component,
	    final Object targetObject) {
	final Element contextMenuElement = element.getChild("contextmenu");
	if (contextMenuElement == null) {
	    return;
	}
	component.addMouseListener(new MouseAdapter() {
	    public void mouseClicked(MouseEvent me) {
		if (me.isPopupTrigger()) {
		    popupMenu(me);
		    return;
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
		    String menuTitle = contextMenuElement.getAttributeValue(
			    "title", "No Title");
		    Icon menuIcon = Icons.getIcon(contextMenuElement
			    .getAttributeValue("icon", "NoIcon"));
		    List actions = contextMenuElement.getChildren();
		    OculaMenu menu = new OculaMenu(menuTitle, menuIcon);
		    for (Iterator j = actions.iterator(); j.hasNext();) {
			final Element actionElement = (Element) j.next();
			String actionElementName = actionElement
				.getAttributeValue("name", "No Name");
			Icon actionElementIcon = Icons.getIcon(actionElement
				.getAttributeValue("icon", "NoIcon"));
			JMenuItem item = new JMenuItem(actionElementName,
				actionElementIcon);
			item.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				if (targetObject != null) {
				    ocula.putContext("selectedObject",
					    targetObject);
				}
				ocula.putContext("event", ae);
				ocula.getActionRunner().runAction(actionElement);
				if (targetObject != null) {
				    ocula.removeKey("selectedObject");
				}
				ocula.removeKey("event");
			    }
			});
			menu.add(item);
		    }
		    menu.show(component, e.getX(), e.getY());
		}

		catch (Exception ex) {
		    log.error("Exception in mousepressed handler", ex);
		}
	    }
	});
    }
    
    /**
     * Parses the &lt;action&gt; tag and it adds the appropriate action
     * listener to the button received. When the listener is invoked,
     * it adds targetObject to the context and runs the action in the
     * action element.
     * 
     * @param element Element containing an action element.
     * @param button JButton where the action listener should be added.
     * @param targetObject Object that should be added to the context before
     * running the action.
     */
    public void parseAction(Element element, final JButton button, final
	    Object targetObject) {
	final Element actionElement = element.getChild("action");
	
	if (actionElement == null) {
	    log.debug("No <action> element");
	    return;
	}
	
	log.debug("Adding an action listener.");
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		log.debug("Action");
		if (targetObject != null) {
		    ocula.putContext("selectedObject", targetObject);
		}
		ocula.putContext("event", e);
		ocula.getActionRunner().runAction(actionElement);
		
		if (targetObject != null) {
		    ocula.removeKey("selectedObject");
		}
		ocula.removeKey("event");
	    }
	});
    }
}
