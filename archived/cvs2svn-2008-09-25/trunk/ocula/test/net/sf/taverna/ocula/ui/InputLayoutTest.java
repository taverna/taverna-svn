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
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import junit.framework.TestCase;

/**
 * Tests for InputLayout.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
public class InputLayoutTest extends TestCase {

    JFrame frame;
    ResultSetPanel panel;
    InputLayout inputLayout;
    JLabel userNameLabel;
    JTextField userNameField;
    JLabel passwordLabel;
    JPasswordField passwordField;
    JLabel firstNameLabel;
    JTextField firstNameField;
    JLabel lastNameLabel;
    JTextField lastNameField;
    JButton submitButton;
    JButton cancelButton;
    JButton testButton;
    
    int hGap;
    int vGap;
    int cols;
    
    public void setUp() {
	frame = new JFrame();
	frame.setSize(400, 400);
	panel = new ResultSetPanel("Login", Icons.getIcon("password.png"));
	userNameLabel = new JLabel("User name:");
	userNameField = new JTextField();
	passwordLabel = new JLabel("Password:");
	passwordField = new JPasswordField();
	firstNameLabel = new JLabel("First name:");
	lastNameLabel = new JLabel("Last name:");
	lastNameField = new JTextField();
	firstNameField = new JTextField();
	submitButton = new JButton("Submit");
	cancelButton = new JButton("Cancel");
	testButton = new JButton("Test");
	hGap = 10;
	vGap = 5;
	cols = 2;
    }
    public void testDividerWith2Cols() {
	cols = 2;
	inputLayout = new InputLayout(cols, hGap, vGap);
	panel.getContents().setLayout(inputLayout);
	frame.getContentPane().add(panel);
	int width = userNameLabel.getPreferredSize().width;
	addFourItems();
	int correctDivider = width + hGap;
	inputLayout.setComponents(Arrays.asList(panel.getContents().getComponents()));
	int actualDivider = inputLayout.getDivider(0);
	String errorMessage = "Actual divider is " + actualDivider + ", the " +
			"correct one is " + correctDivider;
	assertTrue(errorMessage, actualDivider == correctDivider);
    }
    
    public void testDividerWith4Cols() {
	cols = 4;
	inputLayout = new InputLayout(cols, hGap, vGap);
	panel.getContents().setLayout(inputLayout);
	frame.getContentPane().add(panel);
	int[] widths = new int[2];
	widths[0] = firstNameLabel.getPreferredSize().width;
	widths[1] = lastNameLabel.getPreferredSize().width;
	addEightItems();
	inputLayout.setComponents(Arrays.asList(panel.getContents().getComponents()));
	for (int i = 0; i < 2; ++i) {
	    int correctDivider = widths[i] + hGap;
	    int actualDivider = inputLayout.getDivider(i);
	    String errorMessage = "[" + i + "] Actual divider is "
		    + actualDivider + ", the correct one is "
		    + correctDivider;
	    assertTrue(errorMessage, actualDivider == correctDivider);
	}
    }
    
    public void testPreferredLayoutSizeWith2Cols() {
	cols = 2;
	inputLayout = new InputLayout(cols, hGap, vGap);
	panel.getContents().setLayout(inputLayout);
	frame.getContentPane().add(panel);
	Insets insets = panel.getContents().getInsets();
	int correctHeight = (passwordField.getPreferredSize().height * 2) + vGap
		+ insets.top + insets.bottom;
	int correctWidth = userNameLabel.getPreferredSize().width + hGap +
		insets.left + insets.right + userNameField.getPreferredSize().width;
	addFourItems();
	Dimension correctSize = new Dimension(correctWidth, correctHeight);
	Dimension actualSize = inputLayout.preferredLayoutSize(panel.getContents());
	String errorMessage = "Actual size: " +  actualSize + ", correct size: " + correctSize;
	assertTrue(errorMessage, actualSize.equals(correctSize));
    }
    
    public void testPreferredLayoutSizeWith4Cols() {
	cols = 4;
	inputLayout = new InputLayout(cols, hGap, vGap);
	panel.getContents().setLayout(inputLayout);
	frame.getContentPane().add(panel);
	Insets insets = panel.getContents().getInsets();
	int correctHeight = (passwordField.getPreferredSize().height * 2) + vGap
		+ insets.top + insets.bottom;

	int correctWidth = firstNameLabel.getPreferredSize().width + hGap + 
		firstNameField.getPreferredSize().width + hGap +
		lastNameLabel.getPreferredSize().width + hGap +
		lastNameField.getPreferredSize().width + insets.left +
		insets.right;
	addEightItems();
	Dimension correctSize = new Dimension(correctWidth, correctHeight);
	Dimension actualSize = inputLayout.preferredLayoutSize(panel.getContents());
	String errorMessage = "Actual size: " +  actualSize + ", correct size: " + correctSize;
	assertTrue(errorMessage, actualSize.equals(correctSize));
    }
    
    public void testLayoutContainerWith2Cols() {
	cols = 2;
	inputLayout = new InputLayout(cols, hGap, vGap);
	panel.getContents().setLayout(inputLayout);
	frame.getContentPane().add(panel);
	addFourItems();
	Insets insets = panel.getContents().getInsets();
	Rectangle[] correctBounds = new Rectangle[4];
	Dimension d = userNameField.getPreferredSize();
	int width = userNameLabel.getPreferredSize().width;
	frame.setVisible(true);
	int w = panel.getContents().getWidth() - insets.left - insets.right - (width + hGap);
	correctBounds[0] = new Rectangle(insets.left, insets.top, width, d.height);
	correctBounds[1] = new Rectangle(insets.left + width + hGap, insets.top, w, d.height);
	int y = insets.top + d.height + vGap;
	correctBounds[2] = new Rectangle(insets.left, y, width, d.height);
	correctBounds[3] = new Rectangle(insets.left + width + hGap, y, w, d.height);
	for (int i = 0, c = panel.getContents().getComponentCount(); i < c; ++i) {
	    Component component = panel.getContents().getComponent(i);
	    String errorMessage = "[" + i + "] Correct bounds: " + correctBounds[i] + "," +
	    		"Actual bounds: " + component.getBounds();
	    assertTrue(errorMessage, component.getBounds().equals(correctBounds[i]));
	}
    }
    
    public void testLayoutContainerWith4ColsAnd6Items() {
	cols = 4;
	inputLayout = new InputLayout(cols, hGap, vGap);
	panel.getContents().setLayout(inputLayout);
	frame.getContentPane().add(panel);
	addSixItems();
	Insets insets = panel.getContents().getInsets();
	Rectangle[] correctBounds = new Rectangle[8];
	Dimension d = userNameField.getPreferredSize();
	int firstColumnWidth = firstNameLabel.getPreferredSize().width;
	int secondColumnWidth = passwordLabel.getPreferredSize().width;
	frame.setVisible(true);
	int w = panel.getContents().getWidth() - insets.left - insets.right -
		(firstColumnWidth + hGap) - (secondColumnWidth + hGap) - hGap;
	w /= 2;
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
	for (int i = 0, c = panel.getContents().getComponentCount(); i < c; ++i) {
	    Component component = panel.getContents().getComponent(i);
	    String errorMessage = "[" + i + "] Correct bounds: " + correctBounds[i] + "," +
	    		"Actual bounds: " + component.getBounds();
	    assertTrue(errorMessage, component.getBounds().equals(correctBounds[i]));
	}
    }
    
    public void testLayoutContainerWith4ColsAnd8Items() {
	cols = 4;
	inputLayout = new InputLayout(cols, hGap, vGap);
	panel.getContents().setLayout(inputLayout);
	frame.getContentPane().add(panel);
	addEightItems();
	Insets insets = panel.getContents().getInsets();
	Rectangle[] correctBounds = new Rectangle[8];
	Dimension d = userNameField.getPreferredSize();
	int firstColumnWidth = firstNameLabel.getPreferredSize().width;
	int secondColumnWidth = lastNameLabel.getPreferredSize().width;
	frame.setVisible(true);
	int w = panel.getContents().getWidth() - insets.left - insets.right -
		(firstColumnWidth + hGap) - (secondColumnWidth + hGap) - hGap;
	w /= 2;
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
	    Component component = panel.getContents().getComponent(i);
	    String errorMessage = "[" + i + "] Correct bounds: " + correctBounds[i] + "," +
	    		"Actual bounds: " + component.getBounds();
	    assertTrue(errorMessage, component.getBounds().equals(correctBounds[i]));
	}
    }
    
    public void testButtonsLayout() {
	cols = 4;
	inputLayout = new InputLayout(cols, hGap, vGap);
	panel.getContents().setLayout(inputLayout);
	frame.getContentPane().add(panel);
	Insets insets = panel.getContents().getInsets();
	addButtons();
	Dimension buttonSize = submitButton.getPreferredSize();
	int totalButtonWidth = buttonSize.width * 3 + hGap * 2;
	int correctHeight = buttonSize.height;
	Rectangle[] correctBounds = new Rectangle[3];
	frame.setVisible(true);
	int totalWidth = panel.getContents().getWidth() - insets.left - insets.right;
	int x = insets.left + Math.max(0, (totalWidth - totalButtonWidth) / 2);
	
	for (int i = 0; i < 3; ++i) {
	    Component component = panel.getContents().getComponent(i);
	    correctBounds[i] = new Rectangle(x, insets.top, buttonSize.width, correctHeight);
	    x += buttonSize.width + hGap;
	    String errorMessage = "[" + i + "] Correct bounds: " + correctBounds[i] + "," +
		"Actual bounds: " + component.getBounds();
	    assertTrue(errorMessage, component.getBounds().equals(correctBounds[i]));	    
	}
	
    }
    
    public void testJPanelLayout() {
	cols = 4;
	inputLayout = new InputLayout(cols, hGap, vGap);
	panel.getContents().setLayout(inputLayout);
	frame.getContentPane().add(panel);
	Insets insets = panel.getContents().getInsets();
	addButtons();
	JPanel newPanel = new JPanel();
	panel.getContents().add(newPanel);
	Dimension buttonSize = submitButton.getPreferredSize();
	frame.setVisible(true);
	int totalWidth = panel.getContents().getSize().width - insets.left - insets.right;
	int y = insets.top + buttonSize.height + vGap;
	Rectangle correctBounds = new Rectangle(insets.left, y,
		totalWidth, newPanel.getPreferredSize().height);
	String errorMessage = "Correct bounds: " + correctBounds + "," +
		"Actual bounds: " + newPanel.getBounds();
	assertTrue(errorMessage, newPanel.getBounds().equals(correctBounds));	    
	
    }
    
    public void testIllegalColsNumber() {
	cols = 3;
	try {
	    inputLayout = new InputLayout(cols, hGap, vGap);
	}
	catch(IllegalArgumentException iae) {
	    return;
	}
	fail("IllegalArgumentException was not thrown");
    }
    
    public void testIllegalVGap() {
	vGap = -1;
	try {
	    inputLayout = new InputLayout(cols, hGap, vGap);
	}
	catch(IllegalArgumentException iae) {
	    return;
	}
	fail("IllegalArgumentException was not thrown");
    }
    
    public void testIllegalHGap() {
	hGap = -1;
	try {
	    inputLayout = new InputLayout(cols, hGap, vGap);
	}
	catch(IllegalArgumentException iae) {
	    return;
	}
	fail("IllegalArgumentException was not thrown");
    }
    
    private void addButtons() {
	panel.getContents().add(submitButton);
	panel.getContents().add(cancelButton);
	panel.getContents().add(testButton);
    }
    
    private void addSixItems() {
	panel.getContents().add(userNameLabel);
	panel.getContents().add(userNameField);
	panel.getContents().add(passwordLabel);
	panel.getContents().add(passwordField);
	panel.getContents().add(firstNameLabel);
	panel.getContents().add(firstNameField);
    }
    private void addFourItems() {
	panel.getContents().add(userNameLabel);
	panel.getContents().add(userNameField);
	panel.getContents().add(passwordLabel);
	panel.getContents().add(passwordField);
    }
    
    private void addEightItems() {
	addSixItems();
	panel.getContents().add(lastNameLabel);
	panel.getContents().add(lastNameField);
    }
}
