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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.*;
import java.util.*;

/**
 * A single JPanel containing text areas for the name and description
 * of the generated API
 * @author Tom Oinn
 */
public class DescriptionPanel extends JPanel {

    APIDescription description;
    
    public DescriptionPanel(APIDescription description) {
	super(new BorderLayout());
	setOpaque(false);
	this.description = description;
	add(Box.createRigidArea(new Dimension(10,10)), BorderLayout.EAST);
	add(Box.createRigidArea(new Dimension(10,10)), BorderLayout.WEST);
	add(Box.createRigidArea(new Dimension(10,10)), BorderLayout.SOUTH);
	add(Box.createRigidArea(new Dimension(10,10)), BorderLayout.NORTH);

	JPanel internalPanel = new JPanel();
	internalPanel.setLayout(new BoxLayout(internalPanel, BoxLayout.PAGE_AXIS));
	internalPanel.setOpaque(false);

	JTextField nameWidget = new JTextField(description.name);
	JPanel namePanel = new JPanel();
	namePanel.setOpaque(false);
	namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.LINE_AXIS));
	JLabel nameLabel = new JLabel("Name for API");
	nameLabel.setOpaque(false);
	namePanel.add(nameLabel);
	namePanel.add(Box.createRigidArea(new Dimension(10,10)));
	namePanel.add(nameWidget);
	namePanel.add(Box.createHorizontalGlue());
	namePanel.setMaximumSize(new Dimension(6000,20));
	
	JPanel p = new JPanel();
	p.setOpaque(false);
	p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
	JLabel l = new JLabel("Description for API");
	l.setOpaque(false);
	p.add(l);
	p.add(Box.createHorizontalGlue());
	p.setMaximumSize(new Dimension(6000,20));
	
	JTextArea descriptionWidget = new JTextArea(description.description);
	descriptionWidget.setLineWrap(true);

	internalPanel.add(namePanel);
	internalPanel.add(Box.createRigidArea(new Dimension(10,10)));
	internalPanel.add(p);
	internalPanel.add(Box.createRigidArea(new Dimension(4,4)));
	internalPanel.add(new JScrollPane(descriptionWidget));
	
	add(internalPanel, BorderLayout.CENTER);
	
	// Add document listeners to update the APIDescription whenever
	// the user changes anything
	descriptionWidget.getDocument().addDocumentListener(new DocumentListener() {
		public void insertUpdate(DocumentEvent e) {
		    try {
			Document d = e.getDocument();
			DescriptionPanel.this.description.description = (d.getText(0, d.getLength()));
		    }
		    catch (BadLocationException ble) {
			//
		    }
		}
		public void removeUpdate(DocumentEvent e) {
		    try {
			Document d = e.getDocument();
			DescriptionPanel.this.description.description = (d.getText(0, d.getLength()));
		    }
		    catch (BadLocationException ble) {
			//
		    }
		}
		public void changedUpdate(DocumentEvent e) {
		    try {
			Document d = e.getDocument();
			DescriptionPanel.this.description.description = (d.getText(0, d.getLength()));
		    }
		    catch (BadLocationException ble) {
			//
		    }
		}
	    });
	nameWidget.getDocument().addDocumentListener(new DocumentListener() {
		public void insertUpdate(DocumentEvent e) {
		    try {
			Document d = e.getDocument();
			DescriptionPanel.this.description.name = (d.getText(0, d.getLength()));
		    }
		    catch (BadLocationException ble) {
			//
		    }
		}
		public void removeUpdate(DocumentEvent e) {
		    try {
			Document d = e.getDocument();
			DescriptionPanel.this.description.name = (d.getText(0, d.getLength()));
		    }
		    catch (BadLocationException ble) {
			//
		    }
		}
		public void changedUpdate(DocumentEvent e) {
		    try {
			Document d = e.getDocument();
			DescriptionPanel.this.description.name = (d.getText(0, d.getLength()));
		    }
		    catch (BadLocationException ble) {
			//
		    }
		}
	    });
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
    
}
