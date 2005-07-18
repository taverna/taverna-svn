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
import javax.swing.border.*;

/**
 * Subclass of JPopupMenu with Ocula styling
 * @author Tom Oinn
 */
public class OculaMenu extends JPopupMenu {

    public OculaMenu(String title, Icon titleIcon) {
	//setBorder(BorderFactory.createLineBorder(ColourSet.getColour("ocula.contextborder"),2));
	//setOpaque(false);
	//setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ColourSet.getColour("ocula.contextforeground"),1),
	//					     BorderFactory.createLineBorder(ColourSet.getColour("ocula.contextborder"),1)));
	
	setBackground(ColourSet.getColour("ocula.contextbackground"));
	/**JPanel titlePanel = new JPanel(new BorderLayout());
	   titlePanel.setOpaque(true);
	   titlePanel.setBackground(ColourSet.getColour("ocula.contextborder"));
	   JLabel titleLabel = new JLabel(title, titleIcon, SwingConstants.LEFT);
	   titleLabel.setForeground(ColourSet.getColour("ocula.contextforeground"));
	   titleLabel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	   titlePanel.add(titleLabel);
	   add(titlePanel);
	*/
	TitleLabel titleLabel = new TitleLabel(title, titleIcon);
	titleLabel.setBackground(ColourSet.getColour("ocula.panelborder"));
	titleLabel.setForeground(ColourSet.getColour("ocula.panelforeground"));
	add(titleLabel);
    }

    public void addSeperator() {
	JLabel sep = new JLabel();
	sep.setMaximumSize(new Dimension(6000,2));
	sep.setMinimumSize(new Dimension(0,2));
	sep.setOpaque(true);
	sep.setBackground(ColourSet.getColour("ocula.contextborder"));
	sep.setBorder(BorderFactory.createEmptyBorder());
	add(sep);
    }

}
