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
package net.sf.taverna.t2.activities.sadi.menu;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.activities.sadi.SADIActivityPort;
import net.sf.taverna.t2.ui.menu.AbstractMenuSection;
import net.sf.taverna.t2.ui.menu.ContextualMenuComponent;
import net.sf.taverna.t2.ui.menu.ContextualSelection;
import net.sf.taverna.t2.ui.menu.DefaultContextualMenu;

/**
 * 
 * 
 * @author David Withers
 */
public class SADIServiceDiscoveryMenuSection extends AbstractMenuSection implements
		ContextualMenuComponent {

	private static final String SADI_SERVICE_DISCOVERY = "SADI Service Discovery";
	public static final URI sadiServiceDiscoverySection = URI
			.create("http://taverna.sf.net/2009/contextMenu/sadiServiceDiscovery");
	private ContextualSelection contextualSelection;

	public SADIServiceDiscoveryMenuSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 50, sadiServiceDiscoverySection);
	}

	public boolean isEnabled() {
		return getContextualSelection().getSelection() instanceof SADIActivityPort;
	}

	@Override
	@SuppressWarnings("serial")
	protected Action createAction() {
		return new AbstractAction(SADI_SERVICE_DISCOVERY) {
			public void actionPerformed(ActionEvent e) {
			}
		};
	}

	/**
	 * Returns the contextualSelection.
	 * 
	 * @return the contextualSelection
	 */
	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	/**
	 * Sets the value of contextualSelection.
	 * 
	 * @param contextualSelection
	 *            the new value for contextualSelection
	 */
	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
	}

}
