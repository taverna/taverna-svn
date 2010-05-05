/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.sadi.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.DebugGraphics;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link SADIViewUtils}.
 *
 * @author David Withers
 */
public class SADIViewUtilsTest {

	private class MyGraphics extends DebugGraphics {
		public Color color;
		public Rectangle rectangle;

		public void drawLine(int x1, int y1, int x2, int y2) {
			rectangle = new Rectangle(x1, y1, x2, y2);
		}

		public void setColor(Color aColor) {
			color = aColor;
		}
	}

	private JComponent component;
	
	private MyGraphics graphics;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		component = new JPanel();
		graphics = new MyGraphics();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.views.SADIViewUtils#addDivider(javax.swing.JComponent, int, boolean)}.
	 */
	@Test
	public void testAddDivider() {
		SADIViewUtils.addDivider(component, SwingConstants.TOP, false);
		assertEquals(new Insets(5, 0, 0, 0), component.getBorder().getBorderInsets(component));
		assertFalse(component.getBorder().isBorderOpaque());
		component.getBorder().paintBorder(component, graphics, 0, 0, 1, 1);
		assertEquals(Color.LIGHT_GRAY, graphics.color);
		assertEquals(new Rectangle(0, 0, 1, 0), graphics.rectangle);

		SADIViewUtils.addDivider(component, SwingConstants.BOTTOM, false);
		assertEquals(new Insets(0, 0, 5, 0), component.getBorder().getBorderInsets(component));
		assertFalse(component.getBorder().isBorderOpaque());
		component.getBorder().paintBorder(component, graphics, 0, 0, 1, 1);
		assertEquals( Color.LIGHT_GRAY, graphics.color);
		assertEquals(new Rectangle(0, 0, 1, 0), graphics.rectangle);

		SADIViewUtils.addDivider(component, SwingConstants.TOP, true);
		assertEquals(new Insets(5, 0, 0, 0), component.getBorder().getBorderInsets(component));
		assertFalse(component.getBorder().isBorderOpaque());
		component.getBorder().paintBorder(component, graphics, 0, 0, 1, 1);
		assertEquals(Color.WHITE, graphics.color);
		assertEquals(new Rectangle(0, 1, 1, 1), graphics.rectangle);

		SADIViewUtils.addDivider(component, SwingConstants.BOTTOM, true);
		assertEquals(new Insets(0, 0, 5, 0), component.getBorder().getBorderInsets(component));
		assertFalse(component.getBorder().isBorderOpaque());
		component.getBorder().paintBorder(component, graphics, 0, 0, 1, 1);
		assertEquals(Color.WHITE, graphics.color);
		assertEquals(new Rectangle(0, 0, 1, 0), graphics.rectangle);

	}

}
