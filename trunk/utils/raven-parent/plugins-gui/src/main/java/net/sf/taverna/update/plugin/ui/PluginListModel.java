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
 * Filename           $RCSfile: PluginListModel.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008-04-27 21:16:46 $
 *               by   $Author: stain $
 * Created on 28 Nov 2006
 *****************************************************************/
package net.sf.taverna.update.plugin.ui;

import javax.swing.AbstractListModel;

import net.sf.taverna.update.plugin.PluginManager;
import net.sf.taverna.update.plugin.event.PluginManagerEvent;
import net.sf.taverna.update.plugin.event.PluginManagerListener;

import org.apache.log4j.Logger;

/**
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class PluginListModel extends AbstractListModel implements PluginManagerListener {
	private PluginManager pluginManager;
	
	private static Logger logger = Logger.getLogger(PluginListModel.class);
	
	public PluginListModel(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
		PluginManager.addPluginManagerListener(this);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(int index) {
		return pluginManager.getPlugins().get(index);
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize() {
		return pluginManager.getPlugins().size();
	}
	
	public void pluginAdded(PluginManagerEvent event) {		
		fireIntervalAdded(this, event.getPluginIndex(), event.getPluginIndex());
	}

	public void pluginRemoved(PluginManagerEvent event) {	
		fireIntervalRemoved(this, event.getPluginIndex(), event.getPluginIndex());
	}

	public void pluginChanged(PluginManagerEvent event) {		
		fireContentsChanged(this, event.getPluginIndex(), event.getPluginIndex());
	}

	public void pluginIncompatible(PluginManagerEvent event) {
				
	}
}
