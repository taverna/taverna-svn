/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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
package net.sf.taverna.t2.service.model;

/**
 * A Workflow contains the serialized form of a workflow.
 * 
 * @author David Withers
 */
public class Workflow extends IdentifiableImpl {
	
	private String xml;
	
	private boolean enabled;
	
	/**
	 * Returns the serialized form of a workflow.
	 *
	 * @return the serialized form of a workflow
	 */
	public String getXml() {
		return xml;
	}

	/**
	 * Sets the serialized form of a workflow.
	 *
	 * @param xml the serialized form of a workflow
	 */
	public void setXml(String xml) {
		this.xml = xml;
	}

	/**
	 * Returns the enabled.
	 *
	 * @return the value of enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled.
	 *
	 * @param enabled the new value for enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((xml == null) ? 0 : xml.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Workflow other = (Workflow) obj;
		if (enabled != other.enabled) {
			return false;
		}
		if (xml == null) {
			if (other.xml != null) {
				return false;
			}
		} else if (!xml.equals(other.xml)) {
			return false;
		}
		return true;
	}

}
