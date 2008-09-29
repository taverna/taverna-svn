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
 * Revision           $Revision: 1.9 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-11 16:19:18 $
 *               by   $Author: davidwithers $
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
		Element urlElement = new Element("url");
		if (getUrl() != null) {
			urlElement.addContent(getUrl().toString());
		}
		pluginSiteElement.addContent(urlElement);
		return pluginSiteElement;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PluginSite other = (PluginSite) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
}
