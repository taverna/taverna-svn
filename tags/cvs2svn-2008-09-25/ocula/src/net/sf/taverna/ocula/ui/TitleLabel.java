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
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

/**
 * A label with slanted edges designed to be added to the top of a 
 * frame as a title a la beos style windows.
 * @author Tom Oinn
 */
public class TitleLabel extends JPanel {

    private Color background;
    int pad = 10;
    private JLabel label = null;

    public TitleLabel(String text, Icon icon) {
	super(new FlowLayout(FlowLayout.LEFT,0,3));
	setOpaque(false);
	label = new JLabel(text, icon, SwingConstants.LEFT);
	label.setOpaque(false);
	label.setFont(new Font("SansSerif", Font.BOLD, 12));
	add(Box.createHorizontalStrut(pad));
	add(label);
	add(Box.createHorizontalStrut(pad));
    }
    
    public void setBackground(Color background) {
	this.background = background;
    }

    public void setForeground(Color foregroundColour) {
	if (label != null) {
	    label.setForeground(foregroundColour);
	}
    }

    public Dimension getPreferredSize() {
	return new Dimension(label.getPreferredSize().width + pad * 2, label.getPreferredSize().height + 2);
    }

    public Dimension getMaximumSize() {
	return this.getPreferredSize();
    }

    protected void paintComponent(Graphics g) {
	int width = getWidth();
	int height = getHeight();
	Graphics2D g2d = (Graphics2D)g;
	Map map = new HashMap();
	map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	RenderingHints hints = new RenderingHints(map);
	g2d.setRenderingHints(hints);
	Paint oldPaint = g2d.getPaint();
	g2d.setPaint(this.background);
	GeneralPath path = new GeneralPath();
	path.moveTo(0,height);
	path.lineTo(0,height-pad);
	path.curveTo(0,0,0,0,pad,0);
	path.lineTo(width-pad,0);
	path.curveTo(width,0,width,0,width,pad);
	path.lineTo(width,height);
	path.closePath();
	g2d.fill(path);
	g2d.setPaint(oldPaint);
	super.paintComponent(g);
    }

}
