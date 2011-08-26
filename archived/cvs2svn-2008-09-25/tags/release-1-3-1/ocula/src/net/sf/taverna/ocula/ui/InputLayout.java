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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * This layout deals with 3 types of components. Each is treated differently.
 * 
 * JButtons are simply centered and given the same size.
 * 
 * JScrollPane and JPanel instances occupy the whole width available and their
 * preferred height is respected.
 * 
 * Finally, JTextFields and JLabels are arranged into pairs of columns. The
 * following description uses a 0-based index. Usually, the even columns will
 * contain JLabels and the odd ones will contain JTextFields. The width of the
 * even columns is set to the one of the widest JLabel of that column. The rest
 * of the width is distributed equally amongst the odd columns. Note that the
 * cols parameter is only relevant to JTextFields and JLabels.
 * 
 * @author Ismael Juma
 * 
 */
public class InputLayout implements LayoutManager {

    private int[] dividers;

    private static final int MULTI_LINE_TYPE = 0;
    private static final int BUTTON_TYPE = 1; 
    private static final int COLUMN_TYPE = 2;
    private static final int DEFAULT_H_GAP = 10;
    private static final int DEFAULT_V_GAP = 5;
    private static final int DEFAULT_COLS = 2;
    private int hGap;

    private int vGap;

    private int cols;
    
    /**
     * This List is a container used by the layout manager to pass lists
     * of components of the same type to different methods. It is not really
     * necessary, but avoids having to create an ArrayList frequently.
     */
    private List components;

    /**
     * Creates an instance layout with the default values: 10 for hGap, 5 for
     * vGap and 2 columns.
     */
    public InputLayout() {
	this(DEFAULT_H_GAP, DEFAULT_V_GAP, DEFAULT_COLS);
    }
    
    /**
     * Creates an instance of the layout with default values for the vGap
     * and hGap, but allows the user to set the number of columns.
     * @param cols Number of columns used by the layout. It has to be an
     * even number.
     */
    public InputLayout(int cols) {
	this(cols, DEFAULT_H_GAP, DEFAULT_V_GAP);
    }

    /**
     * Creates an instance of the layout with the values supplied by the user.
     * @param cols Number of columns. This number must be an even number.
     * @param hGap Gap between each column.
     * @param vGap Gap between each row.
     */
    public InputLayout(int cols, int hGap, int vGap) {
	if (cols  % 2 != 0) {
	    throw new IllegalArgumentException("cols must be an even number. It" +
	    		" currently is: " + cols + ".");
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
	components = new ArrayList();
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
	components.clear();
	int w = 0;
	int h = 0;
	int type = -1;
	
	for (int i = 0, c = parent.getComponentCount(); i < c; ++i) {
	    Component comp = parent.getComponent(i);
	    int newType = getLayoutType(comp);
	    
	    if (i == 0) {
		type = newType;
	    }
	    
	    if (type != newType) {
		Dimension d = preferredLayoutSize(type);
		w = Math.max(w, d.width);
		h += d.height + vGap;
		components.clear();
		type = newType;
	    }
	    
	    components.add(comp);
	}
	
	Dimension d = preferredLayoutSize(type);
	w = Math.max(w, d.width);
	h += d.height + vGap;
	
	h -= vGap;
	
	Insets insets = parent.getInsets();
	return new Dimension(w + insets.left + insets.right, h
		+ insets.top + insets.bottom);
    }
    
    public Dimension preferredLayoutSize(int type) {
	
	switch (type) {
	case BUTTON_TYPE:
	    return buttonsPreferredLayoutSize(type);
	    
	case COLUMN_TYPE:
	    return columnsPreferredLayoutSize(type);
	
	case MULTI_LINE_TYPE:
	    return multiLinePrefferredLayoutSize(type);
	}
	throw new IllegalArgumentException("Illegal type " + type);

    }
    
    private Dimension buttonsPreferredLayoutSize(int type) {
	Dimension d = getMaximumDimension();
	int w = d.width + hGap;
	int h = d.height;
	return new Dimension(w * components.size() - hGap, h);
    }
    
    private Dimension columnsPreferredLayoutSize(int type) {
	int dividerTotal = getDividerTotal();
	int w = 0;
	int h = 0;
	Dimension d = null;
	for (int i = 1, c = components.size(); i < c; i += cols) {
	    Component comp = (Component) components.get(i);
	    d = comp.getPreferredSize();
	    w = Math.max(w, d.width);
	    h += d.height + vGap;
	}
	w *= (cols / 2);
	h -= vGap;

	return new Dimension(dividerTotal + w, h);
    }
    
    private Dimension multiLinePrefferredLayoutSize(int type) {
	int h = 0;
	int w = 0;
	Dimension d = null;
	for (Iterator it = components.iterator(); it.hasNext();) {
	    Component comp = (Component) it.next();
	    d = comp.getPreferredSize();
	    w = Math.max(w, d.width);
	    h += d.height + vGap;
	}
	h -= vGap;
	return new Dimension(w, h);
    }
    
    private Dimension getMaximumDimension() {
	int w = 0;
	int h = 0;
	Dimension d = null;
	for (Iterator it = components.iterator(); it.hasNext(); ) {
	    Component comp = (Component) it.next();
	    d = comp.getPreferredSize();
	    w = Math.max(w, d.width);
	    h = Math.max(h, d.height);
	}
	return new Dimension(w, h);
    }
    
    private int getLayoutType(Component component) {
	if (component instanceof AbstractButton) {
	    return BUTTON_TYPE;
	}
	else if (component instanceof JPanel || component instanceof JScrollPane) {
	    return MULTI_LINE_TYPE;
	}
	else {
	    return COLUMN_TYPE;
	}
    }

    public Dimension minimumLayoutSize(Container parent) {
	return preferredLayoutSize(parent);
    }

    public void layoutContainer(Container parent) {
	components.clear();
	int type = -1;
	
	dividers = getDividers();

	Insets insets = parent.getInsets();
	int w = parent.getWidth() - insets.left - insets.right;
	int x = insets.left;
	int y = insets.top;
	
	for (int i = 0, c = parent.getComponentCount(); i < c; ++i) {
	    Component comp = parent.getComponent(i);
	    int newType = getLayoutType(comp);
	    if (i == 0) {
		type = newType;
	    }
	    
	    if (type != newType) {
		y = layoutComponents(type, x, y, w);
		components.clear();
		type = newType;
	    }
	    
	    components.add(comp);
	}
	
	y = layoutComponents(type, x, y, w);
	components.clear();
    }
    
    private int layoutComponents(int type, int x, int y, int w) {
	switch (type) {
	case MULTI_LINE_TYPE:
	    return layoutMultiLine(type, x, y, w);
	    
	case COLUMN_TYPE:
	    return layoutColumns(type, x, y, w);
	    
	case BUTTON_TYPE:
	    return layoutButtons(type, x, y, w);
	}
	
	throw new IllegalArgumentException("Illegal type " + type);
    }
    
    private int layoutMultiLine(int type, int x, int y, int w) {
	for (Iterator it = components.iterator(); it.hasNext();) {
	    Component comp = (Component) it.next();
	    Dimension d = comp.getPreferredSize();
	    comp.setBounds(x, y, w, d.height);
	    y += d.height + vGap;
	}
	return y;
    }
    
    private int layoutColumns(int type, int x, int y, int w) {
	int totalDivider = getDividerTotal();
	int ww = (w - totalDivider) / (cols / 2);

	for (int i = 1, c = components.size(); i < c; i += cols) {
	    int xx = x;
	    Dimension d = null;
	    for (int j = 0, k = 0; j < cols && (i + j < c); ++k, j += 2) {
		Component comp1 = (Component) components.get(i + j - 1);
		Component comp2 = (Component) components.get(i + j);
		d = comp2.getPreferredSize();
		comp1.setBounds(xx, y, dividers[k] - hGap, d.height);
		xx += dividers[k];
		comp2.setBounds(xx, y, ww, d.height);
		xx += hGap + ww;
	    }
	    y += d.height + vGap;
	}
	return y;
    }
    
    private int layoutButtons(int type, int x, int y, int w) {
	Dimension d = getMaximumDimension();
	int ww = d.width * components.size() + hGap * (components.size() - 1);
	int xx = x + Math.max(0, (w - ww) / 2);

	for (Iterator it = components.iterator(); it.hasNext();) {
	    Component comp = (Component) it.next();
	    comp.setBounds(xx, y, d.width, d.height);
	    xx += d.width + hGap;
	}
	return y + d.height + vGap;
    }

    /**
         * Gets the hGap property.
         * 
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
	int divider = 0;
	for (int i = cols * index / 2, c = components.size(); i < c; i += cols) {
	    Component comp = (Component) components.get(i);
	    Dimension d = comp.getPreferredSize();
	    divider = Math.max(divider, d.width);
	}
	divider += hGap;
	return divider;
    }
    
    protected int[] getDividers() {
	for (int i = 0; i < (cols / 2); ++i)
	    dividers[i] = getDivider(i);
	
	return dividers;
    }
    
    protected int getDividerTotal() {
	dividers = getDividers();
	int dividerTotal = 0;
	for (int i = 0; i < (cols / 2); ++i) {
	    dividerTotal += dividers[i];
	}
	int extraHGap = hGap * ((cols / 2) - 1);
	dividerTotal = dividerTotal + extraHGap;
	return dividerTotal;
    }
    
    void setComponents(List components) {
	this.components = components;
    }
    
}
