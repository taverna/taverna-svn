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
package net.sf.taverna.raven.spi;

import java.util.Set;

import net.sf.taverna.raven.repository.Artifact;

/**
 * Filter a set of Artifact objects according to some criteria
 * 
 * @author Tom Oinn
 */
public interface ArtifactFilter {

	public abstract void addArtifactFilterListener(
			ArtifactFilterListener listener);

	/**
	 * Given a set of Artifacts returns a subset according to the filtering
	 * criteria defined by implementations of this interface.
	 * 
	 * @param artifacts
	 *            List of Artifacts to filter
	 * @return filtered subset of the input
	 */
	public abstract Set<Artifact> filter(Set<Artifact> artifacts);

	public abstract void removeArtifactFilterListener(
			ArtifactFilterListener listener);

}
