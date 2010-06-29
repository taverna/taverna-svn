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
 * Filename           $RCSfile: PluginManagerListener.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/09/04 14:51:50 $
 *               by   $Author: sowen70 $
 * Created on 30 Nov 2006
 *****************************************************************/
package net.sf.taverna.raven.plugins.event;

import java.util.EventListener;

/**
 * PluginManagerListener defines the interface for an object that listens to
 * changes in a PluginManager.
 * 
 * @author David Withers
 */
public interface PluginManagerListener extends EventListener {

	/**
	 * Invoked when a plugin is added.
	 *
	 * @param event
	 */
	public void pluginAdded(PluginManagerEvent event);

	/**
	 * Invoked when a plugin is removed.
	 *
	 * @param event
	 */
	public void pluginRemoved(PluginManagerEvent event);

	/**
	 * Invoked when a plugin is changed.
	 *
	 * @param event
	 */
	public void pluginChanged(PluginManagerEvent event);
	
	/**
	 * Invoked when a plugin is found to be incompatible with the current running
	 * version of Taverna.
	 * 
	 * @param event
	 */
	public void pluginIncompatible(PluginManagerEvent event);

}
