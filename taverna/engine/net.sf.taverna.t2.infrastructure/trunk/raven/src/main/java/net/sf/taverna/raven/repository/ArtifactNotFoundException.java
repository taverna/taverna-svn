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
 * Thrown when an attempt to locate a remote module in a repository fails for
 * some reason or when a reference is made to an Artifact that cannot be
 * resolved within the current local repository.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 */
public class ArtifactNotFoundException extends RavenException {

	private static final long serialVersionUID = 1L;
	private final Artifact artifact;

	public ArtifactNotFoundException(Artifact artifact) {
		super("Could not find artifact " + artifact);
		this.artifact = artifact;
	}

	public ArtifactNotFoundException(String msg) {
		super(msg);
		this.artifact = null;
	}
	
	public ArtifactNotFoundException(Artifact artifact, String msg) {
		super("Could not find artifact " + artifact + ": " + msg);
		this.artifact = artifact;
	}

	public Artifact getArtifact() {
		return artifact;
	}

}
