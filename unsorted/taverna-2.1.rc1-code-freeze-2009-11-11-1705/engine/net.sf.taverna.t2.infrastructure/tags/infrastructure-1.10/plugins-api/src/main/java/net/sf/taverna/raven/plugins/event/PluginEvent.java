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
 * Filename           $RCSfile: PluginEvent.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/09/04 14:51:48 $
 *               by   $Author: sowen70 $
 * Created on 1 Dec 2006
 *****************************************************************/
package net.sf.taverna.raven.plugins.event;

import java.util.EventObject;

import net.sf.taverna.raven.plugins.Plugin;

/**
 * An event to notify listeners that a <code>Plugin</code> has changed.
 *
 * @author David Withers
 */
public class PluginEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private Plugin plugin;
	
	private int action;
	
	public static final int NONE = 0;
	
	public static final int ENABLED = 1;
	
	public static final int DISABLED = 2;
	
	/**
	 * Constructs an instance of PluginEvent.
	 *
	 * @param source the source of the event
	 * @param plugin the <code>Plugin</code> that has changed
	 */
	public PluginEvent(Object source, Plugin plugin) {
		this(source, plugin, NONE);
	}

	/**
	 * Constructs an instance of PluginEvent.
	 *
	 * @param source the source of the event
	 * @param plugin the <code>Plugin</code> that has changed
	 * @param action
	 */
	public PluginEvent(Object source, Plugin plugin, int action) {
		super(source);
		this.plugin = plugin;
		this.action = action;
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
	 * Returns the action.
	 *
	 * @return the action
	 */
	public int getAction() {
		return action;
	}

}
