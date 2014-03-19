/*******************************************************************************
 * Copyright (C) 2009 Ingo Wassink of University of Twente, Netherlands and
 * The University of Manchester   
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

/**
 * @author Ingo Wassink
 * @author Ian Dunlop
 * @author Alan R Williams
 */
package net.sf.taverna.t2.activities.rshell;

import net.sf.taverna.t2.activities.rshell.RshellPortTypes.SemanticTypes;

public class RShellPortSymanticTypeBean {

	private String name;
	
	private SemanticTypes symanticType;

	/**
	 * Returns the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the symanticType.
	 *
	 * @return the symanticType
	 */
	public SemanticTypes getSymanticType() {
		return symanticType;
	}

	/**
	 * Sets the symanticType.
	 *
	 * @param symanticType the new symanticType
	 */
	public void setSymanticType(SemanticTypes symanticType) {
		this.symanticType = symanticType;
	}

}
