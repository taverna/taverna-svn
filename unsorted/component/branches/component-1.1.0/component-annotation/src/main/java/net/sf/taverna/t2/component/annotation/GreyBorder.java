package net.sf.taverna.t2.component.annotation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.AbstractBorder;

@SuppressWarnings("serial")
class GreyBorder extends AbstractBorder {
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		g.setColor(Color.GRAY);
		g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
	}
}
