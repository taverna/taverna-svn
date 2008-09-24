/*
 * Copyright 2005 Tom Oinn, EMBL-EBI and University of Manchester.
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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

/**
 * Abstract superclass for result containers within Ocula. A result 
 * container is a swing component which aggregates and organizes the
 * objects returned from a method call, most probably a method call
 * on one of the context objects but that's not defined here.
 * @author Tom Oinn
 * @author Ismael Juma
 */
public class ResultSetPanel extends OculaPanel {

    private JProgressBar progressBar;
    
    public ResultSetPanel(String name, Icon icon) {
	super();
	setUpContents();
	add(createLabelPanel(name, icon), BorderLayout.NORTH);
	add(createProgressPanel(), BorderLayout.SOUTH);
    }
    
    public JProgressBar getProgressBar() {
	return this.progressBar;
    }
    
    /**
     * Configures the look of the contents area of the component.
     */
    protected void setUpContents() {
	contentsPanel.setBackground(ColourSet
		.getColour("ocula.panelbackground"));
	contentsPanel.setOpaque(true);
	Border border = BorderFactory.createCompoundBorder(BorderFactory
		.createLineBorder(ColourSet.getColour("ocula.panelborder"), 2),
		BorderFactory.createEmptyBorder(2, 2, 2, 2));
	contentsPanel.setBorder(border);
    }


    private JPanel createProgressPanel() {
	JPanel progressPanel = new JPanel();
	progressPanel.setBackground(ColourSet.getColour("ocula.panelborder"));
	progressPanel.setMaximumSize(new Dimension(6000,25));
	progressPanel.setPreferredSize(new Dimension(110, 20));
	progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.LINE_AXIS));
	progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
	progressBar.setBackground(ColourSet.getColour("ocula.panelborder"));
	progressBar.setOpaque(false);
	progressBar.setMaximumSize(new Dimension(100,15));
	progressBar.setPreferredSize(new Dimension(100,15));
	progressBar.setMinimumSize(new Dimension(100,15));
	progressPanel.add(Box.createRigidArea(new Dimension(4,4)));
	progressPanel.add(progressBar);
	progressPanel.add(Box.createHorizontalGlue());
	return progressPanel;
    }

    private JPanel createLabelPanel(String title, Icon icon) {
	JPanel labelPanel = new JPanel();
	labelPanel.setOpaque(false);
	labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
	TitleLabel label = new TitleLabel(title, icon);
	label.setBackground(ColourSet.getColour("ocula.panelborder"));
	label.setForeground(ColourSet.getColour("ocula.panelforeground"));
	labelPanel.setMaximumSize(new Dimension(6000,20));
	labelPanel.add(label);
	labelPanel.add(Box.createHorizontalGlue());
	return labelPanel;
    }

}
