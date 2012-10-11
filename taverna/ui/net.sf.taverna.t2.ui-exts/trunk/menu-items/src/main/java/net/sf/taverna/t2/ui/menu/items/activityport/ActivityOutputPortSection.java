/**********************************************************************
 * Copyright (C) 2007-2009 The University of Manchester
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
 **********************************************************************/
package net.sf.taverna.t2.ui.menu.items.activityport;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;
import net.sf.taverna.t2.ui.menu.ContextualMenuComponent;
import net.sf.taverna.t2.ui.menu.ContextualSelection;
import net.sf.taverna.t2.ui.menu.DefaultContextualMenu;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;

public class ActivityOutputPortSection extends AbstractMenuSection implements
		ContextualMenuComponent {

	private static final String ACTIVITY_OUTPUT_PORT = "Service output port: ";
	public static final URI activityOutputPortSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/activityOutputPort");
	private ContextualSelection contextualSelection;

	public ActivityOutputPortSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 10,
				activityOutputPortSection);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return getContextualSelection().getSelection() instanceof OutputProcessorPort;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		this.action = null;
	}

	@Override
	protected Action createAction() {
		OutputProcessorPort port = (OutputProcessorPort) getContextualSelection().getSelection();
		String name = ACTIVITY_OUTPUT_PORT + port.getName();
		return new DummyAction(name);
	}

}
