/***********************************************************************
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
 ***********************************************************************/
package net.sf.taverna.t2.platform.plugin;

import java.net.URL;
import java.util.List;

import net.sf.taverna.t2.platform.plugin.generated.PluginDescription;

/**
 * Parser and loader used to obtain PluginDescription instances from
 * PluginIdentifier and list of plugin repositories
 * 
 * @author Tom Oinn
 * 
 */
public interface PluginDescriptionParser {

	/**
	 * Get a plugin description given the supplied identifier and list of
	 * repositories. The repositories may be ignored if the description has
	 * previously been downloaded and cached.
	 * 
	 * @param id
	 *            the PluginIdentifier for this plug-in description request
	 * @param pluginRepositories
	 *            a list of URLs pointing to plug-in repositories
	 * @return a PluginDescription object
	 * @throws PluginException
	 *             if any unrecoverable problems occur during the fetch or parse
	 *             of the plug-in description file.
	 */
	public PluginDescription getDescription(PluginIdentifier id,
			List<URL> pluginRepositories) throws PluginException;

}
