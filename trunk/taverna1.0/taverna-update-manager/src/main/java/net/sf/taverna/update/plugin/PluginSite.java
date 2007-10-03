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
 * Filename           $RCSfile: PluginSite.java,v $
 * Revision           $Revision: 1.8 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-10-03 12:10:52 $
 *               by   $Author: sowen70 $
 * Created on 28 Nov 2006
 *****************************************************************/
package net.sf.taverna.update.plugin;

import java.net.MalformedURLException;
import java.net.URL;

import org.jdom.Element;

/**
 *
 * @author David Withers
 */
public class PluginSite {
	private String name;
	private URL url;		

	/**
	 * Constructs an instance of PluginSite.
	 *
	 * @param name the name of the plugin site
	 * @param url the url of the plugin site
	 */
	public PluginSite(String name, URL url) {
		this.name = name;
		this.url = url;
	}	

	/**
	 * Returns the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the url.
	 *
	 * @return the url
	 */
	public URL getUrl() {
		return url;
	}
	
	public String toString() {
		return "Plugin site " + getUrl();
	}
	
	/**
	 * Creates a <code>PluginSite</code> from an XML element.
	 * 
	 * @param pluginSiteElement the XML element
	 * @return a new <code>PluginSite</code>
	 */
	public static PluginSite fromXml(Element pluginSiteElement) throws MalformedURLException {
		String name = pluginSiteElement.getChildTextTrim("name");
		String urlString = pluginSiteElement.getChildTextTrim("url");
		if (!urlString.endsWith("/")) {
			urlString = urlString + "/";
		}
		URL url = new URL(urlString);		
		return new PluginSite(name, url);
	}

	/**
	 * Creates an XML element from this <code>PluginSite</code>.
	 * 
	 * @return an XML element for this <code>PluginSite</code>
	 */
	public Element toXml() {
		Element pluginSiteElement = new Element("pluginSite");
		pluginSiteElement.addContent(new Element("name").addContent(getName()));
		pluginSiteElement.addContent(new Element("url").addContent(getUrl().toString()));
		return pluginSiteElement;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PluginSite)) return false;
		PluginSite site = (PluginSite)obj;
		return (site.getUrl().equals(this.getUrl()) && site.getName().equals(this.getName()));
	}

	@Override
	public int hashCode() {
		return (this.getUrl().toExternalForm()+this.getName()).hashCode();
	}
	
	
	
}
