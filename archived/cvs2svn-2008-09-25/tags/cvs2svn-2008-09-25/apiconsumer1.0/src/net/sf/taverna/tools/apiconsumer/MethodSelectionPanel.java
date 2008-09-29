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

package net.sf.taverna.tools.apiconsumer;

import com.sun.javadoc.*;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.*;
import java.util.*;

/**
 * A single JPanel containing checkboxes which will add or remove
 * MethodDoc objects from the specified APIDescription when selected
 * or deselected
 * @author Tom Oinn
 */
public class MethodSelectionPanel extends JPanel {
    
    private APIDescription description;
    private ClassDoc classdoc;
    private JCheckBox[] boxes;
    private ClassTree tree;

    /**
     * Create a new MethodSelectionPanel which will display
     * all available non static public methods from the
     * specified ClassDoc and allow the insertion into or
     * removal from the specified APIDescription object
     */
    public MethodSelectionPanel(APIDescription description,
				ClassDoc classdoc,
				ClassTree tree) {
	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	this.description = description;
	this.classdoc = classdoc;
	this.tree = tree;
	int rows = 0;
	int column = 0;
	List methods = getNonStaticMethods();
	List staticMethods = getStaticMethods();
	boxes = new JCheckBox[methods.size()+staticMethods.size()+getConstructors().size()];
	int j = 0;

	JPanel boxPanel = new JPanel();
	for (Iterator i = methods.iterator(); i.hasNext();) {
	    JCheckBox cb = createCheckBox((MethodDoc)i.next());
	    boxes[j++] = cb;
	    boxPanel.add(cb);
	    column++;
	    if (column == 3) {
		column = 0;
		rows++;
	    }
	}
	if (column > 0) {
	    rows++;
	    for (int i = column; i < 3; i++) {
		boxPanel.add(new JLabel());
	    }
	}
	boxPanel.setLayout(new GridLayout(rows, 3));
	boxPanel.setMaximumSize(new Dimension(6000,15*rows));
	boxPanel.setOpaque(false);

	JPanel staticPanel = new JPanel();
	column = 0;
	rows = 0;
	for (Iterator i = staticMethods.iterator(); i.hasNext();) {
	    JCheckBox cb = createCheckBox((MethodDoc)i.next());
	    boxes[j++] = cb;
	    staticPanel.add(cb);
	    column++;
	    if (column == 3) {
		column = 0;
		rows++;
	    }
	}
	if (column > 0) {
	    rows++;
	    for (int i = column; i < 3; i++) {
		staticPanel.add(new JLabel());
	    }
	}
	staticPanel.setLayout(new GridLayout(rows, 3));
	staticPanel.setMaximumSize(new Dimension(6000,15*rows));
	staticPanel.setOpaque(false);

	JPanel constructorPanel = new JPanel();
	column = 0;
	rows = 0;
	for (Iterator i = getConstructors().iterator(); i.hasNext();) {
	    JCheckBox cb = createCheckBox((ConstructorDoc)i.next());
	    boxes[j++] = cb;
	    constructorPanel.add(cb);
	    column++;
	    if (column == 1) {
		column = 0;
		rows++;
	    }
	}
	if (column > 0) {
	    rows++;
	    for (int i = column; i < 1; i++) {
		constructorPanel.add(new JLabel());
	    }
	}
	constructorPanel.setLayout(new GridLayout(rows, 1));
	constructorPanel.setMaximumSize(new Dimension(6000,15*rows));
	constructorPanel.setOpaque(false);

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
	JButton selectAll = new JButton("Select all");
	selectAll.setOpaque(false);
	selectAll.setMaximumSize(new Dimension(200,20));
	selectAll.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    for (int i = 0; i < boxes.length; i++) {
			boxes[i].setSelected(true);
		    }
		}
	    });
	JButton clearAll = new JButton("Clear all");
	clearAll.setOpaque(false);
	clearAll.setMaximumSize(new Dimension(200,20));
	clearAll.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    for (int i = 0; i < boxes.length; i++) {
			boxes[i].setSelected(false);
		    }
		}
	    });
	buttonPanel.add(selectAll);
	buttonPanel.add(Box.createRigidArea(new Dimension(5,5)));
	buttonPanel.add(clearAll);
	buttonPanel.add(Box.createHorizontalGlue());
	buttonPanel.setMaximumSize(new Dimension(6000,20));
	buttonPanel.setOpaque(false);

	// Create top level UI
	JPanel headingPanel = new JPanel();
	headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.LINE_AXIS));
	headingPanel.add(new JLabel("Select methods to appear in Taverna's services panel :"));
	headingPanel.add(Box.createHorizontalGlue());
	headingPanel.setMaximumSize(new Dimension(6000,20));
	headingPanel.setOpaque(false);
	
	JPanel headingPanel2 = new JPanel();
	headingPanel2.setLayout(new BoxLayout(headingPanel2, BoxLayout.LINE_AXIS));
	headingPanel2.add(new JLabel("Static methods :"));
	headingPanel2.add(Box.createHorizontalGlue());
	headingPanel2.setMaximumSize(new Dimension(6000,20));
	headingPanel2.setOpaque(false);

	JPanel headingPanel3 = new JPanel();
	headingPanel3.setLayout(new BoxLayout(headingPanel3, BoxLayout.LINE_AXIS));
	headingPanel3.add(new JLabel("Constructors :"));
	headingPanel3.add(Box.createHorizontalGlue());
	headingPanel3.setMaximumSize(new Dimension(6000,20));
	headingPanel3.setOpaque(false);
	

	add(Box.createRigidArea(new Dimension(5,5)));
	add(headingPanel);
	add(Box.createRigidArea(new Dimension(5,5)));
	add(boxPanel);
	add(Box.createRigidArea(new Dimension(5,5)));
	add(headingPanel2);
	add(Box.createRigidArea(new Dimension(5,5)));
	add(staticPanel);
	add(Box.createRigidArea(new Dimension(5,5)));
	add(headingPanel3);
	add(Box.createRigidArea(new Dimension(5,5)));
	add(constructorPanel);
	add(Box.createRigidArea(new Dimension(5,5)));
	add(buttonPanel);
	    
	add(Box.createVerticalGlue());
	setOpaque(false);
    }
    
    // Apply a graduated coloured background
    protected void paintComponent(Graphics g) {
	final int width = getWidth();
	final int height = getHeight();
	Graphics2D g2d = (Graphics2D)g;
	Paint oldPaint = g2d.getPaint();
	g2d.setPaint(new GradientPaint(0,0,new Color(200,200,255),width,0,Color.WHITE));
	g2d.fillRect(0,0,width,height);
	g2d.setPaint(oldPaint);
	super.paintComponent(g);
    }
    
    /**
     * Create a JCheckBox bound to the specified MethodDoc
     */
    private JCheckBox createCheckBox(MethodDoc method) {
	final MethodDoc theMethod = method;
	JCheckBox cb = new JCheckBox(method.name());
	cb.setSelected(description.contains(classdoc, method));
	cb.setContentAreaFilled(false);
	cb.setMaximumSize(new Dimension(200,15));
	cb.setPreferredSize(new Dimension(200,15));
	cb.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    if (e.getStateChange() == ItemEvent.SELECTED) {
			description.add(classdoc, theMethod);
			MethodSelectionPanel.this.tree.repaint();
		    }
		    else {
			description.remove(classdoc, theMethod);
			MethodSelectionPanel.this.tree.repaint();
		    }
		}
	    });
	return cb;
    }

    /**
     * Create a JCheckBox bound to the specified ConstructorDoc
     */
    private JCheckBox createCheckBox(ConstructorDoc constructor) {
	final ConstructorDoc theConstructor = constructor;
	// Create the string representing the constructor
	StringBuffer sb = new StringBuffer();
	sb.append("<html><body>(");
	Parameter[] params = theConstructor.parameters();
	for (int j = 0; j < params.length; j++) {
	    Parameter param = params[j];
	    sb.append("<font color=\"green\">"+param.type().typeName()+param.type().dimension()+"</font> <font color=\"purple\">"+param.name()+"</font>");
	    if (j < params.length-1) {
		sb.append(", ");
	    }
	}
	sb.append(")</html></body>");
	JCheckBox cb = new JCheckBox(sb.toString());
	cb.setSelected(description.contains(classdoc, theConstructor));
	cb.setContentAreaFilled(false);
	cb.setMaximumSize(new Dimension(200,15));
	cb.setPreferredSize(new Dimension(200,15));
	cb.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    if (e.getStateChange() == ItemEvent.SELECTED) {
			description.add(classdoc, theConstructor);
			MethodSelectionPanel.this.tree.repaint();
		    }
		    else {
			description.remove(classdoc, theConstructor);
			MethodSelectionPanel.this.tree.repaint();
		    }
		}
	    });
	return cb;
    }
    /**
     * Return a List containing all MethodDoc objects
     * corresponding to public non static methods
     */
    private List getNonStaticMethods() {
	List result = new ArrayList();
	MethodDoc[] methods = classdoc.methods();
	for (int i = 0; i < methods.length; i++) {
	    MethodDoc method = methods[i];
	    if (method.isPublic() &&
		method.isStatic() == false) {
		result.add(method);
	    }
	}
	return result;
    }

    /**
     * As above but only return the static methods
     */
    private List getStaticMethods() {
	List result = new ArrayList();
	MethodDoc[] methods = classdoc.methods();
	for (int i = 0; i < methods.length; i++) {
	    MethodDoc method = methods[i];
	    if (method.isPublic() &&
		method.isStatic()) {
		result.add(method);
	    }
	}
	return result;
    }

    /** 
     * Fetch constructors (public only)
     */
    private List getConstructors() {
	List result = new ArrayList();
	ConstructorDoc[] constructors = classdoc.constructors();
	for (int i = 0; i < constructors.length; i++) {
	    if (constructors[i].isPublic()) {
		result.add(constructors[i]);
	    }
	}
	return result;
    }

}
