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

package net.sf.taverna.ocula.ui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Superclass for containers within Ocula. This is a very simple swing component
 * that has the appropriate look.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public class OculaPanel extends JPanel {

    protected JPanel contentsPanel;
    
    public OculaPanel() {
	super(new BorderLayout());
	setOpaque(false);
	contentsPanel = new JPanel();
	setUpContentsDelegate();
	add(contentsPanel, BorderLayout.CENTER);
	setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	
    }
    
    /**
     * Removes the current panel in the contents area and adds the one received
     * as a parameter.
     * 
     * @param panel
     */
    public void setContents(JPanel panel) {
	remove(contentsPanel);
	contentsPanel = panel;
	add(contentsPanel, BorderLayout.CENTER);
    }
    
    /**
     * Configures the contents area with the appropriate look.
     */
    protected void setUpContents() {
	setUpContentsDelegate();
    }
    
    /**
     * This method is responsible for actually configuring the contents area
     * with the appropriate look. It is necessary because it needs to be called
     * from the constructor, and as such it should be final.
     */
    private void setUpContentsDelegate() {
	contentsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
	contentsPanel.setOpaque(false);
    }
    
    public JPanel getContents() {
	return this.contentsPanel;
    }
}
