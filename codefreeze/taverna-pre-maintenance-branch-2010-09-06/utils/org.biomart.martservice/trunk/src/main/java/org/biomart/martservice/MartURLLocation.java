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
 * Filename           $RCSfile: MartURLLocation.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007/10/04 14:15:59 $
 *               by   $Author: davidwithers $
 * Created on 17-Mar-2006
 *****************************************************************/
package org.biomart.martservice;

import org.ensembl.mart.lib.config.MartLocation;

/**
 * The MartURLLocation contained by the MartRegistry returned by a BioMart web
 * service.
 * 
 * @author David Withers
 */
public class MartURLLocation implements MartLocation {
	private static final String URL_TYPE = "URL";

	private String database;

	private boolean defaultValue;

	private String displayName;

	private String host;

	private String includeDatasets;

	private String martUser;

	private String name;

	private String path;

	private int port;

	private String serverVirtualSchema;

	private String virtualSchema;

	private boolean visible;

	private boolean redirect;

	/**
	 * Returns the database.
	 *
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * Sets the database.
	 *
	 * @param database the new database
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * Returns true if the default flag is set.
	 * 
	 * @return true if the default flag is set
	 */
	public boolean isDefault() {
		return defaultValue;
	}

	/**
	 * Sets the default flag.
	 * 
	 * @param defaultValue
	 *            the value of the default flag
	 */
	public void setDefault(boolean defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Returns the displayName.
	 * 
	 * @return the displayName.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the displayName.
	 * 
	 * @param displayName
	 *            the displayName to set.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Returns the host.
	 * 
	 * @return the host.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the host.
	 * 
	 * @param host
	 *            the host to set.
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Returns the includeDatasets.
	 *
	 * @return the includeDatasets
	 */
	public String getIncludeDatasets() {
		return includeDatasets;
	}

	/**
	 * Sets the includeDatasets.
	 *
	 * @param includeDatasets the new includeDatasets
	 */
	public void setIncludeDatasets(String includeDatasets) {
		this.includeDatasets = includeDatasets;
	}

	/**
	 * Returns the martUser.
	 *
	 * @return the martUser
	 */
	public String getMartUser() {
		return martUser;
	}

	/**
	 * Sets the martUser.
	 *
	 * @param martUser the new martUser
	 */
	public void setMartUser(String martUser) {
		this.martUser = martUser;
	}

	/**
	 * Returns the name
	 * 
	 * @return the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the path.
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path.
	 *
	 * @param path the new path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Returns the port.
	 * 
	 * @return the port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port.
	 * 
	 * @param port
	 *            the port to set.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Returns the serverVirtualSchema.
	 * 
	 * @return the serverVirtualSchema.
	 */
	public String getServerVirtualSchema() {
		return serverVirtualSchema;
	}

	/**
	 * Sets the serverVirtualSchema.
	 * 
	 * @param serverVirtualSchema
	 *            the serverVirtualSchema to set.
	 */
	public void setServerVirtualSchema(String serverVirtualSchema) {
		this.serverVirtualSchema = serverVirtualSchema;
	}

	/**
	 * Returns the virtualSchema.
	 * 
	 * @return the virtualSchema.
	 */
	public String getVirtualSchema() {
		return virtualSchema;
	}

	/**
	 * Sets the virtualSchema.
	 * 
	 * @param virtualSchema
	 *            the virtualSchema to set.
	 */
	public void setVirtualSchema(String virtualSchema) {
		this.virtualSchema = virtualSchema;
	}

	/**
	 * Returns true if the location is visible.
	 * 
	 * @return Returns the visible.
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible
	 *            The visible to set.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Returns true if this location is redirected.
	 * 
	 * @return the redirect
	 */
	public boolean isRedirect() {
		return redirect;
	}

	/**
	 * @param redirect the redirect to set
	 */
	public void setRedirect(boolean redirect) {
		this.redirect = redirect;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.mart.lib.config.MartLocation#getType()
	 */
	public String getType() {
		return URL_TYPE;
	}

	/**
	 * Returns the display name.
	 * 
	 * @return the display name
	 */
	public String toString() {
		return getDisplayName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((database == null) ? 0 : database.hashCode());
		result = prime * result + (defaultValue ? 1231 : 1237);
		result = prime * result
				+ ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result
				+ ((includeDatasets == null) ? 0 : includeDatasets.hashCode());
		result = prime * result
				+ ((martUser == null) ? 0 : martUser.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + port;
		result = prime * result + (redirect ? 1231 : 1237);
		result = prime
				* result
				+ ((serverVirtualSchema == null) ? 0 : serverVirtualSchema
						.hashCode());
		result = prime * result
				+ ((virtualSchema == null) ? 0 : virtualSchema.hashCode());
		result = prime * result + (visible ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MartURLLocation other = (MartURLLocation) obj;
		if (database == null) {
			if (other.database != null)
				return false;
		} else if (!database.equals(other.database))
			return false;
		if (defaultValue != other.defaultValue)
			return false;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (includeDatasets == null) {
			if (other.includeDatasets != null)
				return false;
		} else if (!includeDatasets.equals(other.includeDatasets))
			return false;
		if (martUser == null) {
			if (other.martUser != null)
				return false;
		} else if (!martUser.equals(other.martUser))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (port != other.port)
			return false;
		if (redirect != other.redirect)
			return false;
		if (serverVirtualSchema == null) {
			if (other.serverVirtualSchema != null)
				return false;
		} else if (!serverVirtualSchema.equals(other.serverVirtualSchema))
			return false;
		if (virtualSchema == null) {
			if (other.virtualSchema != null)
				return false;
		} else if (!virtualSchema.equals(other.virtualSchema))
			return false;
		if (visible != other.visible)
			return false;
		return true;
	}

}
