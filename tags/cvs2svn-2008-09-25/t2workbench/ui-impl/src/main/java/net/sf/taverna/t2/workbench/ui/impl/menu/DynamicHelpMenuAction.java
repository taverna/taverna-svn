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
package net.sf.taverna.t2.workbench.ui.impl.menu;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URL;

import javax.help.CSH;
import javax.help.CSH.DisplayHelpFromSource;
import javax.help.HelpBroker;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

public class DynamicHelpMenuAction extends AbstractMenuAction {

	private static Logger logger = Logger
			.getLogger(DynamicHelpMenuAction.class);

	public DynamicHelpMenuAction() {
		super(HelpMenu.HELP_URI, 10);
	}

	/**
	 * Find the helpset file and create a HelpSet object
	 * 
	 * @throws HelpSetException
	 */
	public HelpSet getHelpSet(String helpsetfile) throws HelpSetException {
		// FIXME: Find helpsets from plugins
		HelpSet hs = null;
		ClassLoader cl = this.getClass().getClassLoader();
		URL hsURL = HelpSet.findHelpSet(cl, helpsetfile);
		hs = new HelpSet(null, hsURL);
		return hs;

	}

	@Override
	protected Action createAction() {
		return new DynamicHelpAction();
	}

	private final class DynamicHelpAction extends AbstractAction {
		private DynamicHelpAction() {
			super("Dynamic help");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));
		}

		public void actionPerformed(ActionEvent e) {
			HelpSet hs;
			try {
				hs = getHelpSet("sample.hs");
			} catch (HelpSetException ex) {
				logger.warn("Could not load helpset", ex);
				JOptionPane.showMessageDialog(null, "Could not load helpset",
						"Could not find help", JOptionPane.ERROR_MESSAGE);
				return;
			}
			HelpBroker hb = hs.createHelpBroker();
			new DisplayHelpFromSource(hb).actionPerformed(e);
		}
	}

}
