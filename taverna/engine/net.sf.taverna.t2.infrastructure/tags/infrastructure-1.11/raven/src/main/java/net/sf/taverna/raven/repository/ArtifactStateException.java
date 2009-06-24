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
package net.sf.taverna.raven.repository;

import net.sf.taverna.raven.RavenException;

/**
 * Thrown when an operation attempt to perform on an artifact that isn't in the
 * right state relative to a particular repository
 * 
 * @author Tom Oinn
 */
public class ArtifactStateException extends RavenException {

	private static final long serialVersionUID = 1L;

	private ArtifactStatus state;
	private ArtifactStatus[] validStates;

	public ArtifactStateException(ArtifactStatus state,
			ArtifactStatus[] validStates) {
		this.state = state;
		this.validStates = validStates;
	}

	public ArtifactStatus getState() {
		return this.state;
	}

	public ArtifactStatus[] getValidStates() {
		return this.validStates;
	}

}
