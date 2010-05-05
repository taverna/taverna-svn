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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.RDFNode;

import net.sf.taverna.t2.activities.sadi.SADIActivityInputPort;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

/**
 * 
 *
 * @author David Withers
 */
public class SADIInputPortContextualView extends ContextualView {

	private static final long serialVersionUID = 1L;

	private final SADIActivityInputPort inputPort;
	private JPanel viewPanel;

	/**
	 * Constructs a new SADIInputPortContextualView.
	 * @param inputPort
	 */
	public SADIInputPortContextualView(SADIActivityInputPort inputPort) {
		this.inputPort = inputPort;
		initView();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getMainFrame()
	 */
	@Override
	public JComponent getMainFrame() {
		refreshView();
		return viewPanel;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getPreferredPosition()
	 */
	@Override
	public int getPreferredPosition() {
		return 100;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getViewTitle()
	 */
	@Override
	public String getViewTitle() {
		return "SADI input : " + inputPort.getName();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#refreshView()
	 */
	@Override
	public void refreshView() {
		viewPanel = new JPanel(new GridBagLayout());
		
		viewPanel.setBackground(Color.WHITE);
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1d;
		
		OntClass inputClass  = inputPort.getOntClass();
		c.insets = new Insets(5, 5, 5, 5);
		viewPanel.add(new JLabel(inputClass.getLocalName()), c);
		
		for (RDFNode comment : inputClass.listComments(null).toList()) {
			c.insets = new Insets(5, 15, 5, 5);
			viewPanel.add(new JLabel(comment.toString()), c);
		}
	}

}
