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
 * Last modified on   $Date: 2006-12-05 12:24:28 $
 *               by   $Author: davidwithers $
 * Created on 30 Nov 2006
 *****************************************************************/
package net.sf.taverna.update.plugin.event;

import java.util.EventObject;

import net.sf.taverna.update.plugin.Plugin;

/**
 *
 * @author David Withers
 */
public class PluginManagerEvent extends EventObject {
	private Plugin plugin;
	
	public PluginManagerEvent(Object source, Plugin plugin) {
		super(source);
		this.plugin = plugin;
	}

	/**
	 * Returns the plugin.
	 *
	 * @return the plugin
	 */
	public Plugin getPlugin() {
		return plugin;
	}
	
}
