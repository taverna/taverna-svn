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
 * Filename           $RCSfile: TavernaPluginSite.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-01-24 16:45:47 $
 *               by   $Author: sowen70 $
 * Created on 12 Dec 2006
 *****************************************************************/
package net.sf.taverna.update.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Extension to PluginSite specifally for the main Taverna plugin site.
 * This will not get entered into the plugins/plugin-sites.xml, and also supports a
 * list of alternative url's for fail-over should the mygrid site fail.
 * 
 * @author Stuart Owen
 *
 */

public class TavernaPluginSite extends PluginSite {
	
	private static Logger logger = Logger.getLogger(TavernaPluginSite.class);
	private URL workingURL = null;
	
	private List<URL> urls;
	
	public TavernaPluginSite(String name, URL [] urls) {
		super(name,urls[0]);
		this.urls=new ArrayList<URL>();
		for (URL url : urls) this.urls.add(url);
	}

	/**
	 * checks each url until it returns a working one, or the null if they all fail.
	 * If a working url is found then this url is stored an returned for subsequent calls.
	 */	
	public URL getUrl() {		
		if (workingURL!=null) {
			if (logger.isDebugEnabled())
				logger.debug("Using known working URL:"+workingURL +" from list: "+urls);
			return workingURL;
		}
		URL pluginsURL=null;
		for (URL url : urls) {
			try {
				pluginsURL=new URL(url,"pluginlist.xml");
				URLConnection con=pluginsURL.openConnection();
				InputStream stream=con.getInputStream();
				stream.close();
				workingURL=url;
				return url;
			}
			catch(IOException e) {
				logger.warn("Unable opening stream to "+pluginsURL+", will use next mirror");
			}
		}
		logger.warn("Unable to connect to Taverna plugin site. None of the candidate URLs worked:"+urls);
		return null;
	}
	
	
}
