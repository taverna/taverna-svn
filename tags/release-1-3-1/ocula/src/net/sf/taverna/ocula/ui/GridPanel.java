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

import javax.swing.*;
import java.awt.*;

/**
 * A JPanel with a GridLayout which behaves correctly when there
 * are empty spaces in the layout. By default the GridLayout
 * just goes completely nuts when you have, for example, five components
 * in a three by two grid. This component allows configuration
 * of the number of columns and automatically inserts blank
 * JLabel components where required to pad the container so it
 * does the layout correctly.
 * @author Tom Oinn
 */
public class GridPanel extends JPanel {

    int cols;
    int dummyCount = 0;

    /**
     * Construct a new GridPanel object with the specified
     * number of columns and containing no components.
     */
    public GridPanel(int cols) {
	super();
	this.cols = cols;
    }
    
    /**
     * Override to do nothing, this component manages its
     * own layout internally - that's the entire point of it
     */
    public void setLayout(LayoutManager l) {
	//
    }

    /**
     * Add a component to the layout - this will remove
     * any dummy components first, add the new component
     * then re-add any required dummy components and reset
     * the layout
     */
    public Component add(Component comp) {
	int currentComponentCount = getComponentCount();
	// Remove all dummy components
	for (int i = 1; i < dummyCount+1; i++) {
	    remove(currentComponentCount - i);
	}
	dummyCount = 0;
	super.add(comp);
	int lastRowCount = (currentComponentCount+1) % cols;
	int rows = (currentComponentCount+1) / cols;
	if (lastRowCount != 0) {
	    // One more row to handle
	    rows++;
	    // Add as many dummy objects as required to make
	    // the last row full
	    for (int i = 0; i < cols - lastRowCount; i++) {
		super.add(new DummyObject());
		dummyCount++;
	    }
	}
	super.setLayout(new GridLayout(rows, cols));
	revalidate();
	return comp;
    }
    
    class DummyObject extends JLabel {
	//
    }

}
