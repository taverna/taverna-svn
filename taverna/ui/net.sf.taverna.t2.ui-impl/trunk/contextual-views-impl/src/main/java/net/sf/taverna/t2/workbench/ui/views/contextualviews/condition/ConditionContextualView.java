/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.condition;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import uk.org.taverna.scufl2.api.core.BlockingControlLink;

/**
 * Contextual view for dataflow's control (condition) links.
 *
 * @author David Withers
 *
 */
public class ConditionContextualView extends ContextualView {

	private static final long serialVersionUID = -894521200616176439L;

	private BlockingControlLink condition;
	private JPanel contitionView;


	public ConditionContextualView(BlockingControlLink condition) {
		this.condition = condition;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		refreshView();
		return contitionView;
	}

	@Override
	public String getViewTitle() {
		return "Control link: " + condition.getBlock().getName()
		+ " runs after " + condition.getUntilFinished().getName();
	}

	@Override
	public void refreshView() {

		contitionView = new JPanel(new FlowLayout(FlowLayout.LEFT));
		contitionView.setBorder(new EmptyBorder(5,5,5,5));
		JLabel label = new JLabel("<html><body><i>No details available.</i></body><html>");
		contitionView.add(label);

	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
