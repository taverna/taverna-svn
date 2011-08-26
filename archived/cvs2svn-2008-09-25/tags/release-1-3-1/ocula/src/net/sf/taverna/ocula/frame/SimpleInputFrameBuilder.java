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

import net.sf.taverna.ocula.ui.InputLayout;
import net.sf.taverna.ocula.ui.OculaPanel;

/**
 * Handles the &lt;simpleinput&gt; element. It produces a subclass of OculaPanel
 * tailored to deal with input fields, labels and buttons. For more details,
 * see {@link net.sf.taverna.ocula.ui.InputLayout}, which is the layout manager
 * responsible for enforcing these properties.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
public class SimpleInputFrameBuilder extends AbstractInputFrameBuilder {

    public String getElementName() {
	return "simpleinput";
    }

    /**
     * Creates a subclass of OculaPanel that uses InputLayout and implements
     * OculaFrame.
     */
    protected OculaPanel createInputFrame() {
	if (hGap < 0 || vGap < 0) {
	    return new SimpleInputFrame(cols);    
	}
	return new SimpleInputFrame(cols, hGap, vGap);
    }
    
    /**
     * Calls the appropriate methods to make the view.
     */
    public OculaFrame makeView() {
	OculaPanel panel = createInputFrame();
	loadView(panel);
	return (OculaFrame) panel;
    }
}

/**
 * Simple subclass of OculaPanel that implements OculaFrame and uses
 * InputLayout.
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
class SimpleInputFrame extends OculaPanel implements OculaFrame	{
    public SimpleInputFrame(int cols) {
	super();
	contentsPanel.setLayout(new InputLayout(cols));
    }
    
    public SimpleInputFrame(int cols, int hGap, int vGap) {
	super();
	contentsPanel.setLayout(new InputLayout(cols, hGap, vGap));
    }
}
