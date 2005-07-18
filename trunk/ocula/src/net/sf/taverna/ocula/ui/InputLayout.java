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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * This layout works by arranging items into pairs of columns. The following
 * description uses a 0-based index. Usually, the even columns will contain
 * JLabels and the odd ones will contain JTextFields. The width of the even
 * columns is set to the one of the widest JLabel of each column. The rest of
 * the width is distributed equally amongst the odd columns.
 * 
 * @author Ismael Juma
 * 
 */
public class InputLayout implements LayoutManager {

    private int[] dividers;

    private static int defaultHGap = 10;
    private static int defaultVGap = 5;
    private static int defaultCols = 2;
    private int hGap;

    private int vGap;

    private int cols;

    /**
     * Creates an instance layout with the default values: 10 for hGap, 5 for
     * vGap and 2 columns.
     */
    public InputLayout() {
	this(defaultHGap, defaultVGap, defaultCols);
    }
    
    /**
     * Creates an instance of the layout with default values for the vGap
     * and hGap, but allows the user to set the number of columns.
     * @param cols Number of columns used by the layout. It has to be an
     * even number.
     */
    public InputLayout(int cols) {
	this(defaultHGap, defaultVGap, cols);
    }

    /**
     * Creates an instance of the layout with the values supplied by the user.
     * @param hGap Gap between each column.
     * @param vGap Gap between each row.
     * @param cols Number of columns. This number must be an even number.
     */
    public InputLayout(int hGap, int vGap, int cols) {
	if (cols  % 2 != 0) {
	    throw new IllegalArgumentException("cols must be an even number.");
	}
	
	if (hGap < 0 || vGap < 0) {
	    throw new IllegalArgumentException("vGap and hGap must not be negative");
	}
	
	this.hGap = hGap;
	this.vGap = vGap;
	this.cols = cols;
	this.dividers = new int[cols];
	for (int i = 0; i < dividers.length; ++i) {
	    dividers[i] = -1;
	}
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
	int dividerTotal = getDividerTotal(parent);

	int w = 0;
	int h = 0;
	for (int i = 1, c = parent.getComponentCount(); i < c; i += cols) {
	    Component comp = parent.getComponent(i);
	    Dimension d = comp.getPreferredSize();
	    w = Math.max(w, d.width);
	    h += d.height + vGap;
	}
	w *= (cols / 2);
	h -= vGap;

	Insets insets = parent.getInsets();
	Dimension d = new Dimension(dividerTotal + w + insets.left + insets.right, h
		+ insets.top + insets.bottom);
	return d;
    }

    public Dimension minimumLayoutSize(Container parent) {
	return preferredLayoutSize(parent);
    }

    public void layoutContainer(Container parent) {
	dividers = getDividers(parent);
	int totalDivider = getDividerTotal(parent);

	Insets insets = parent.getInsets();
	int w = parent.getWidth() - insets.left - insets.right - totalDivider;
	w /= (cols / 2);
	int x = 0;
	int y = insets.top;

	for (int i = 1, c = parent.getComponentCount(); i < c; i += cols) {
	    x = insets.left;
	    Dimension d = null;
	    for (int j = 0, k = 0; j < cols && (i + j < c) ; ++k, j += 2) {
		Component comp1 = parent.getComponent(i + j - 1);
		Component comp2 = parent.getComponent(i + j);
		d = comp2.getPreferredSize();
		comp1.setBounds(x, y, dividers[k] - hGap, d.height);
		x += dividers[k];
		comp2.setBounds(x, y, w, d.height);
		x += hGap + w;
	    }
	    y += d.height + vGap;
	}
    }

    /**
     * Gets the hGap property.
     * @return hGap property.
     */
    public int getHGap() {
	return hGap;
    }

    /**
     * Gets the vGap property.
     * @return vGap property.
     */
    public int getVGap() {
	return vGap;
    }

    protected int getDivider(int index) {
	return dividers[index];
    }

    protected int getDivider(int index, Container parent) {
	if (dividers[index] > 0)
	    return dividers[index];

	int divider = 0;
	for (int i = cols * index / 2, c = parent.getComponentCount(); i < c; i += cols) {
	    Component comp = parent.getComponent(i);
	    Dimension d = comp.getPreferredSize();
	    divider = Math.max(divider, d.width);
	}
	divider += hGap;
	return divider;
    }
    
    protected int[] getDividers(Container parent) {
	for (int i = 0; i < (cols / 2); ++i)
	    dividers[i] = getDivider(i, parent);
	
	return dividers;
    }
    
    protected int getDividerTotal(Container parent) {
	dividers = getDividers(parent);
	int dividerTotal = 0;
	for (int i = 0; i < (cols / 2); ++i) {
	    dividerTotal += dividers[i];
	}
	int extraHGap = hGap * ((cols / 2) - 1);
	dividerTotal = dividerTotal + extraHGap;
	return dividerTotal;
    }
}
