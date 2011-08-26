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
 * Filename           $RCSfile: PluginManagerEvent.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008-04-29 16:16:09 $
 *               by   $Author: stain $
 * Created on 30 Nov 2006
 *****************************************************************/
package net.sf.taverna.raven.plugins.event;

import java.util.EventObject;

import net.sf.taverna.raven.plugins.Plugin;

/**
 * An event to notify listeners that the <code>PluginManager</code> has changed.
 *
 * @author David Withers
 */
public class PluginManagerEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private Plugin plugin;
	private int index;
	
	/**
	 * Constructs an instance of PluginManagerEvent.
	 *
	 * @param source
	 * @param plugin
	 * @param pluginIndex - the index of the plugin in the managers list of plugins
	 */
	public PluginManagerEvent(Object source, Plugin plugin, int pluginIndex) {
		super(source);
		this.plugin = plugin;
		this.index = pluginIndex;
	}

	/**
	 * Returns the plugin.
	 *
	 * @return the plugin
	 */
	public Plugin getPlugin() {
		return plugin;
	}

	/**
	 * 
	 * @return the index of the plugin in the plugin managers internal list
	 */
	public int getPluginIndex() {
		return index;
	}	
}
