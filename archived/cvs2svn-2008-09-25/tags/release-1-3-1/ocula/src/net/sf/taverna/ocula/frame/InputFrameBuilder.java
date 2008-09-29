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

package net.sf.taverna.ocula.frame;

import javax.swing.Icon;

import net.sf.taverna.ocula.ui.Icons;
import net.sf.taverna.ocula.ui.InputLayout;
import net.sf.taverna.ocula.ui.OculaPanel;
import net.sf.taverna.ocula.ui.ResultSetPanel;

/**
 * Handles the &lt;inpit&gt; element. It produces a subclass of ResultSetPanel
 * tailored to deal with input fields, labels and buttons. For more details,
 * see {@link net.sf.taverna.ocula.ui.InputLayout}, which is the layout manager
 * responsible for enforcing these properties.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
public class InputFrameBuilder extends AbstractInputFrameBuilder {

    protected String name;
    protected Icon icon;
    
    public String getElementName() {
	return "input";
    }
    
    /**
     * Creates a subclass of ResultSetPanel that uses InputLayout and implements
     * OculaFrame.
     */
    protected OculaPanel createInputFrame() {
	if (hGap < 0 || vGap < 0) {
	    return new InputFrame(name, icon, cols);
	}
	return new InputFrame(name, icon, cols, hGap, vGap);
    }
    
    /**
     * In addition to the superclass method's functionality, it also processes
     * the "icon" and "name" attributes of the &lt;input&gt; element.
     */
    protected boolean processElement() {
	name = element.getAttributeValue("name");
	if (name == null) {
	    name = "No Name";
	}
	String iconName = element.getAttributeValue("icon");
	if (iconName == null) {
	    iconName = "NoIcon";
	}
	
	icon = Icons.getIcon(iconName);
	return super.processElement();
    }
    
    /**
     * Calls the appropriate methods to make the view and controls the
     * progress bar.
     */
    public OculaFrame makeView() {
	InputFrame inputFrame = (InputFrame) createInputFrame();
	inputFrame.getProgressBar().setValue(0);
	inputFrame.getProgressBar().setIndeterminate(true);
	loadView(inputFrame);
	inputFrame.getProgressBar().setValue(100);
	inputFrame.getProgressBar().setIndeterminate(false);
	return inputFrame;
    }
}

/**
 * Simple subclass of ResultSetPanel that implements OculaFrame and uses
 * InputLayout.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
class InputFrame extends ResultSetPanel implements OculaFrame {
    public InputFrame(String name, Icon icon, int cols) {
	super(name, icon);
	contentsPanel.setLayout(new InputLayout(cols));
    }
    
    public InputFrame(String name, Icon icon, int cols, int hGap, int vGap) {
	super(name, icon);
	contentsPanel.setLayout(new InputLayout(cols, hGap, vGap));
    }
    

}