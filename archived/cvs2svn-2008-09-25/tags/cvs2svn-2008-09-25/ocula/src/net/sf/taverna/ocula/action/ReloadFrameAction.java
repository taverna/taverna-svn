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

package net.sf.taverna.ocula.action;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.ocula.Ocula;
import net.sf.taverna.ocula.frame.FrameAndElement;

import org.jdom.Element;

/**
 * Reloads a specific frame within the page.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 * 
 */
public class ReloadFrameAction implements ActionSPI {

    public String getElementName() {
	return "reloadframe";
    }

    /**
     * Reloads a frame identified by the "key" attribute. This will only work
     * correctly if the frame element to be reloaded also defines a "key"
     * attribute.
     */
    public void act(Ocula ocula, Element element) throws ActionException {
	ocula.getActionRunner().runAction(element);
	String name = element.getAttributeValue("key");
	if (name == null) {
	    throw new ActionException("key attribute must not be null.");
	}
	FrameAndElement fe = (FrameAndElement) ocula.getContext(name);
	if (fe == null) {
	    throw new ActionException("there is no frame in the page with the " +
	    		"given name: " + name);
	}
	JComponent frame = (JComponent) fe.getFrame();
	JPanel mainPanel = ocula.getMainPanel();
	for (int i = 0; i < mainPanel.getComponentCount(); ++i) {
	    Component comp = mainPanel.getComponent(i);
	    if (comp.equals(frame)) {
		mainPanel.remove(i);
		JComponent newComp = recreateFrame(ocula, fe.getElement());
		mainPanel.add(newComp, i);
		mainPanel.revalidate();
		return;
	    }
	}
    }

    /**
     * Parses the element and recreates the frame.
     * @param ocula Ocula instance.
     * @param element Element that contains the frame to be recreated.
     * @return frame created from the element.
     */
    private JComponent recreateFrame(Ocula ocula, Element element) {
	return (JComponent) ocula.getFrameHandler().getFrame(element);
    }
}
