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

import javax.swing.JButton;
import javax.swing.Icon;
import java.awt.Dimension;
import javax.swing.BorderFactory;

/**
 * Trivial JButton subclass that allows configuration of the maximum
 * and preferred size from the constructor, saves on code space (see ScavengerTreePanel
 * in taverna's source for an example of what happens without this)
 * @author Tom Oinn
 */
public class CompactJButton extends JButton {
    
    public CompactJButton(String text, Icon icon, int width, int height) {
	super(text, icon);
	setPreferredSize(new Dimension(width, height));
	setMaximumSize(new Dimension(width, height));
	setOpaque(false);
	setBorder(BorderFactory.createEmptyBorder());
    }
    
    public CompactJButton(Icon icon, int width, int height) {
	super(icon);
	setPreferredSize(new Dimension(width, height));
	setMaximumSize(new Dimension(width, height));
	setOpaque(false);	
	setBorder(BorderFactory.createEmptyBorder());
    }
    
    public CompactJButton(String text, int width, int height) {
	super(text);
	setPreferredSize(new Dimension(width, height));
	setMaximumSize(new Dimension(width, height));
	setOpaque(false);	
	setBorder(BorderFactory.createEmptyBorder());
    }

}
