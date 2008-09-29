/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile$
 * Revision           $Revision$
 * Release status     $State$
 * Last modified on   $Date$
 *               by   $Author$
 * Created on 03-Jul-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.web;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

/**
 * ScavengerHelper associated with Web Scavengers.
 * @author Stuart Owen
 *
 */

public class WebScavengerHelperImpl implements ScavengerHelper, WebScavengerHelper {

	private static Logger logger = Logger.getLogger(WebScavengerHelperImpl.class);

	/**
	 * Always returns an empty set, WebScavengerHelper defaults are found using
	 * getDefaults(ScavengerTree tree).
	 */
	public Set<Scavenger> getDefaults() {
		return new HashSet<Scavenger>();
	}

	/**
	 * Currently doesn't attempt to extract scavengers from the model, so always returns an
	 * empty Set.
	 */
	public Set<Scavenger> getFromModel(ScuflModel model) {
		return new HashSet<Scavenger>();		
	}

	/**
	 * Returns the icon for this scavenger helper
	 */
	public ImageIcon getIcon() {
		return TavernaIcons.webIcon;
	}

	/**
	 * Returns the listener associated with the Scavenger Tree popup menu, to facilitate the adding of
	 * new Web Scavengers
	 */
	public ActionListener getListener(final ScavengerTree theScavengerTree) {		
		ActionListener result = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String rootURL = (String) JOptionPane.showInputDialog(theScavengerTree.getContainingFrame(), "Address of the web page to crawl from?",
						"Web root location", JOptionPane.QUESTION_MESSAGE, null, null,
						"http://cvs.mygrid.org.uk/scufl/");
				if (rootURL != null) {
					try {
						theScavengerTree.addScavenger(new WebScavenger(rootURL, (DefaultTreeModel) theScavengerTree.getModel()));
					} catch (ScavengerCreationException sce) {
						JOptionPane.showMessageDialog(theScavengerTree.getContainingFrame(), "Unable to create scavenger!\n" + sce.getMessage(),
								"Exception!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};

		return result;

	}

	/**
	 * Returns the description text for the menu item for this scavenger
	 */
	public String getScavengerDescription() {
		return "Collect scavengers from web...";
	}

	/**
	 * Returns the defaults, determined by taverna.defaultweb within the mygrid.properties file.
	 * This is specific to WebScavengerHelper since WebScavengers need ScavengerTree in their constuctor.
	 */
	public Set<Scavenger> getDefaults(ScavengerTree tree) {
		Set<Scavenger> result = new HashSet<Scavenger>();
		String urlList = System.getProperty("taverna.defaultweb");
		if (urlList != null) {
			String[] urls = urlList.split("\\s*,\\s*");
			for (int i = 0; i < urls.length; i++) {
				try {
					Scavenger scavenger = new WebScavenger(urls[i], (DefaultTreeModel) tree.getModel());
					result.add(scavenger);
				} catch (ScavengerCreationException sce) {
					logger.error(sce);
				}
			}
		}
		return result;
	}

}
