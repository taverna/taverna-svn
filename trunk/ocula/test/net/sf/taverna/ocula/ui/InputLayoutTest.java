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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import junit.framework.TestCase;

/**
 * Tests for InputLayout.
 * 
 * @author Ismael Juma <ismael@juma.me.uk>
 *
 */
public class InputLayoutTest extends TestCase {

    JFrame frame;
    ResultSetPanel panel;
    InputLayout dialogLayout;
    JLabel userNameLabel;
    JTextField userNameField;
    JLabel passwordLabel;
    JPasswordField passwordField;
    JLabel firstNameLabel;
    JTextField firstNameField;
    JLabel lastNameLabel;
    JTextField lastNameField;
    
    int hGap;
    int vGap;
    int cols;
    
    public void setUp() {
	frame = new JFrame();
	frame.setSize(400, 400);
	panel = new ResultSetPanel("Login", Icons.getIcon("password.png"));
	userNameLabel = new JLabel("User name");
	userNameField = new JTextField();
	passwordLabel = new JLabel("Password:");
	passwordField = new JPasswordField();
	firstNameLabel = new JLabel("First name:");
	lastNameLabel = new JLabel("Last name:");
	lastNameField = new JTextField();
	firstNameField = new JTextField();
	hGap = 10;
	vGap = 5;
	cols = 2;
    }
    public void testDividerWith2Cols() {
	cols = 2;
	dialogLayout = new InputLayout(hGap, vGap, cols);
	panel.getContents().setLayout(dialogLayout);
	frame.getContentPane().add(panel);
	int width = userNameLabel.getPreferredSize().width;
	panel.getContents().add(userNameLabel);
	panel.getContents().add(userNameField);
	panel.getContents().add(passwordLabel);
	panel.getContents().add(passwordField);
	int correctDivider = width + hGap;
	int actualDivider = dialogLayout.getDivider(0, panel.getContents());
	String errorMessage = "Actual divider is " + actualDivider + ", the " +
			"correct one is " + correctDivider;
	assertTrue(errorMessage, actualDivider == correctDivider);
    }
    
    public void testDividerWith4Cols() {
	cols = 4;
	dialogLayout = new InputLayout(hGap, vGap, cols);
	panel.getContents().setLayout(dialogLayout);
	frame.getContentPane().add(panel);
	int[] widths = new int[2];
	widths[0] = firstNameLabel.getPreferredSize().width;
	widths[1] = lastNameLabel.getPreferredSize().width;
	System.out.println(widths[1]);
	System.out.println(passwordLabel.getPreferredSize().width);
	panel.getContents().add(userNameLabel);
	panel.getContents().add(userNameField);
	panel.getContents().add(passwordLabel);
	panel.getContents().add(passwordField);
	panel.getContents().add(firstNameLabel);
	panel.getContents().add(firstNameField);
	panel.getContents().add(lastNameLabel);
	panel.getContents().add(lastNameField);
	for (int i = 0; i < 2; ++i) {
	    int correctDivider = widths[i] + hGap;
	    int actualDivider = dialogLayout.getDivider(i, panel.getContents());
	    String errorMessage = "[" + i + "] Actual divider is "
		    + actualDivider + ", the correct one is "
		    + correctDivider;
	    assertTrue(errorMessage, actualDivider == correctDivider);
	}
    }
    
    public void testPreferredLayoutSizeWith2Cols() {
	cols = 2;
	dialogLayout = new InputLayout(hGap, vGap, cols);
	panel.getContents().setLayout(dialogLayout);
	frame.getContentPane().add(panel);
	Insets insets = panel.getContents().getInsets();
	int correctHeight = (passwordField.getPreferredSize().height * 2) + vGap
		+ insets.top + insets.bottom;
	int correctWidth = userNameLabel.getPreferredSize().width + hGap +
		insets.left + insets.right + userNameField.getPreferredSize().width;
	panel.getContents().add(userNameLabel);
	panel.getContents().add(userNameField);
	panel.getContents().add(passwordLabel);
	panel.getContents().add(passwordField);
	Dimension correctSize = new Dimension(correctWidth, correctHeight);
	Dimension actualSize = dialogLayout.preferredLayoutSize(panel.getContents());
	String errorMessage = "Actual size: " +  actualSize + ", correct size: " + correctSize;
	assertTrue(errorMessage, actualSize.equals(correctSize));
    }
    
    public void testPreferredLayoutSizeWith4Cols() {
	cols = 4;
	dialogLayout = new InputLayout(hGap, vGap, cols);
	panel.getContents().setLayout(dialogLayout);
	frame.getContentPane().add(panel);
	Insets insets = panel.getContents().getInsets();
	int correctHeight = (passwordField.getPreferredSize().height * 2) + vGap
		+ insets.top + insets.bottom;

	int correctWidth = firstNameLabel.getPreferredSize().width + hGap + 
		firstNameField.getPreferredSize().width + hGap +
		lastNameLabel.getPreferredSize().width + hGap +
		lastNameField.getPreferredSize().width + insets.left +
		insets.right;
	panel.getContents().add(userNameLabel);
	panel.getContents().add(userNameField);
	panel.getContents().add(passwordLabel);
	panel.getContents().add(passwordField);
	panel.getContents().add(firstNameLabel);
	panel.getContents().add(firstNameField);
	panel.getContents().add(lastNameLabel);
	panel.getContents().add(lastNameField);
	Dimension correctSize = new Dimension(correctWidth, correctHeight);
	Dimension actualSize = dialogLayout.preferredLayoutSize(panel.getContents());
	String errorMessage = "Actual size: " +  actualSize + ", correct size: " + correctSize;
	assertTrue(errorMessage, actualSize.equals(correctSize));
    }
    
    public void testLayoutContainerWith2Cols() {
	cols = 2;
	dialogLayout = new InputLayout(hGap, vGap, cols);
	panel.getContents().setLayout(dialogLayout);
	frame.getContentPane().add(panel);
	panel.getContents().add(userNameLabel);
	panel.getContents().add(userNameField);
	panel.getContents().add(passwordLabel);
	panel.getContents().add(passwordField);
	Insets insets = panel.getContents().getInsets();
	Rectangle[] correctBounds = new Rectangle[4];
	Dimension d = userNameField.getPreferredSize();
	int width = userNameLabel.getPreferredSize().width;
	frame.setVisible(true);
	int w = panel.getContents().getWidth() - insets.left - insets.right - (width + hGap);
	System.out.println(w);
	correctBounds[0] = new Rectangle(insets.left, insets.top, width, d.height);
	correctBounds[1] = new Rectangle(insets.left + width + hGap, insets.top, w, d.height);
	int y = insets.top + d.height + vGap;
	correctBounds[2] = new Rectangle(insets.left, y, width, d.height);
	correctBounds[3] = new Rectangle(insets.left + width + hGap, y, w, d.height);
	for (int i = 0, c = panel.getContents().getComponentCount(); i < c; ++i) {
	    System.out.println(i);
	    Component component = panel.getContents().getComponent(i);
	    String errorMessage = "[" + i + "] Correct bounds: " + correctBounds[i] + "," +
	    		"Actual bounds: " + component.getBounds();
	    assertTrue(errorMessage, component.getBounds().equals(correctBounds[i]));
	}
    }
    
    public void testLayoutContainerWith4Cols() {
	cols = 4;
	dialogLayout = new InputLayout(hGap, vGap, cols);
	panel.getContents().setLayout(dialogLayout);
	frame.getContentPane().add(panel);
	panel.getContents().add(userNameLabel);
	panel.getContents().add(userNameField);
	panel.getContents().add(passwordLabel);
	panel.getContents().add(passwordField);
	panel.getContents().add(firstNameLabel);
	panel.getContents().add(firstNameField);
	panel.getContents().add(lastNameLabel);
	panel.getContents().add(lastNameField);
	Insets insets = panel.getContents().getInsets();
	Rectangle[] correctBounds = new Rectangle[8];
	Dimension d = userNameField.getPreferredSize();
	int firstColumnWidth = firstNameLabel.getPreferredSize().width;
	int secondColumnWidth = lastNameLabel.getPreferredSize().width;
	frame.setVisible(true);
	int w = panel.getContents().getWidth() - insets.left - insets.right -
		(firstColumnWidth + hGap) - (secondColumnWidth + hGap) - hGap;
	System.out.println(panel.getContents().getWidth() + "-" + insets.left +"-" +insets.right + "-"
		+ firstColumnWidth + " + " + hGap + "-" + secondColumnWidth + "+" + hGap);
	System.out.println(w);
	w /= 2;
	System.out.println(w);
	int x = insets.left;
	correctBounds[0] = new Rectangle(x, insets.top, firstColumnWidth, d.height);
	x += firstColumnWidth + hGap;
	correctBounds[1] = new Rectangle(x, insets.top, w, d.height);
	x += hGap + w; 
	correctBounds[2] = new Rectangle(x, insets.top, secondColumnWidth, d.height);
	x += hGap + secondColumnWidth;
	correctBounds[3] = new Rectangle(x, insets.top, w, d.height);
	int y = insets.top + d.height + vGap;
	x = insets.left;
	correctBounds[4] = new Rectangle(x, y, firstColumnWidth, d.height);
	x += firstColumnWidth + hGap;
	correctBounds[5] = new Rectangle(x, y, w, d.height);
	x += hGap + w;
	correctBounds[6] = new Rectangle(x, y, secondColumnWidth, d.height);
	x += hGap + secondColumnWidth;
	correctBounds[7] = new Rectangle(x, y, w, d.height);
	for (int i = 0, c = panel.getContents().getComponentCount(); i < c; ++i) {
	    System.out.println(i);
	    Component component = panel.getContents().getComponent(i);
	    String errorMessage = "[" + i + "] Correct bounds: " + correctBounds[i] + "," +
	    		"Actual bounds: " + component.getBounds();
	    assertTrue(errorMessage, component.getBounds().equals(correctBounds[i]));
	}
    }
}
