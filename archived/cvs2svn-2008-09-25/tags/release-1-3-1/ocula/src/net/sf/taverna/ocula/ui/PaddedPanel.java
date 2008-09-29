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

import java.awt.*;
import javax.swing.*;

/**
 * A padded and shaded JPanel containing a single component in the middle
 * surrounded by fixed space.
 * @author Tom Oinn
 */
public class PaddedPanel extends JPanel {

    private String colourName;
    
    /**
     * Create a new JPanel subclass with a fixed width border around the 
     * single specified component.
     * @param colourName the name of a colour recognized by the ColourSet
     * class which will be used to create a shaded background fading to a paler
     * shade top to bottom
     * @param padding width of the border around the single component in pixels
     * @param component a JComponent to include within the border, most likely
     * to be wrapped in a JScrollPane 
     */
    public PaddedPanel(String colourName, int padding, Component component) {
	super(new BorderLayout());
	setOpaque(false);
	this.colourName = colourName;
	add(Box.createRigidArea(new Dimension(padding,padding)),
	    BorderLayout.WEST);
	add(Box.createRigidArea(new Dimension(padding,padding)),
	    BorderLayout.SOUTH);
	add(Box.createRigidArea(new Dimension(padding,padding)),
	    BorderLayout.EAST);
	add(Box.createRigidArea(new Dimension(padding,padding)),
	    BorderLayout.NORTH);
	add(component, BorderLayout.CENTER);
    }
    
    protected void paintComponent(Graphics g) {
	final int width = getWidth();
	final int height = getHeight();
	Graphics2D g2d = (Graphics2D)g;
	Paint oldPaint = g2d.getPaint();
	g2d.setPaint(ColourSet.getShadePaint(colourName, new Point(0,0), new Point(0,height)));
	g2d.fillRect(0, 0, width, height);
	g2d.setPaint(oldPaint);
	super.paintComponent(g);
    }

}
