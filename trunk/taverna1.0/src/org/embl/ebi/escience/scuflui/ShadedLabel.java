/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.*;
import javax.swing.*;

/**
 * A JLabel like component with a shaded background
 * @author Tom Oinn
 */
public class ShadedLabel extends JPanel {
    
    public static Color TAVERNA_ORANGE = new Color(238,206,143);
    public static Color TAVERNA_GREEN = new Color(213, 229, 246);
    public static Color TAVERNA_BLUE = new Color(161, 198, 157);
    
    final JLabel label;
    Color colour;

    public ShadedLabel(String text, Color colour) {
	super(new FlowLayout(FlowLayout.LEFT,0,3));
	label = new JLabel("<html><body><b>"+text+"</b></body></html>", SwingConstants.LEFT);
	label.setOpaque(false);
	add(Box.createHorizontalStrut(5));
	add(label);
	add(Box.createHorizontalStrut(5));
	setOpaque(false);
	this.colour = colour;
    }
    
    protected void paintComponent(Graphics g) {
	final int width = getWidth();
	final int height = getHeight();
	Graphics2D g2d = (Graphics2D)g;
	Paint oldPaint = g2d.getPaint();
	g2d.setPaint(new GradientPaint(0, 0, this.colour, width, height, Color.WHITE));
	g2d.fillRect(0, 0, width, height);
	g2d.setPaint(oldPaint);
	super.paintComponent(g);
    }

    public boolean isFocusable() {
	return false;
    }

}
