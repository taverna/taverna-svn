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
 * Filename           $RCSfile: DummyRepository.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008-05-02 16:14:45 $
 *               by   $Author: stain $
 * Created on 18 Oct 2006
 *****************************************************************/
package net.sf.taverna.raven.repository.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.DownloadStatus;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.RepositoryListener;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;

/**
 * Nasty hack, but does allow Taverna to be run within say eclipse for the
 * purposes of debugging and writing new code for a specific component -
 * allowing code changes to be picked up immediately without the need to 'mvn
 * install' and refresh the repository Cache. It does this by replacing the
 * normal LocalRepository and enforsing that the class loader used is always the
 * SystemClassLoader. It also returns a dummy artifact for
 * getArtifacts(ArtifactStatus) when status is Ready, to trick the SpiRegistry
 * into thinking there are new artifacts and thereby looks for new SPI's using
 * teh system classloader.
 * 
 * The use of this repository is triggered by the presence of the system
 * property 'raven.eclipse'
 * 
 * IMPORTANT NOTE: This is a nasty experimental hack for development purposes
 * only and shouldn't be used to run the system for real. Any changes that
 * *appear* to work using this Repository should also be checked by running the
 * code outside as Eclipse.
 * 
 * @author sowen
 */

public class DummyRepository implements Repository {

	private static List<Artifact> artifacts = new ArrayList<Artifact>();

	public void addArtifact(Artifact a) {

	}

	public void addRemoteRepository(URL repositoryURL) {

	}

	public void addRepositoryListener(RepositoryListener l) {

	}

	@SuppressWarnings("unchecked")
	public Artifact artifactForClass(Class c) throws ArtifactNotFoundException {
		return null;
	}

	public List<Artifact> getArtifacts() {
		return artifacts;
	}

	public List<Artifact> getArtifacts(ArtifactStatus s) {
		// adds a dummy artifact, tricks SPIRegistry into looking up SPI
		// registered items (which it gets from the system classpath).
		Artifact a = new BasicArtifact("dummy", "dummy", "dummy");
		if (artifacts.size() == 0) {
			artifacts.add(a);

			// add artifact to profile so it doesn't get filtered out
			Profile p = ProfileFactory.getInstance().getProfile();
			if (p != null) {
				p.addArtifact(a);
			}
		}

		if (s.equals(ArtifactStatus.Ready))
			return artifacts;
		else
			return new ArrayList<Artifact>();
	}

	public DownloadStatus getDownloadStatus(Artifact a)
			throws ArtifactStateException, ArtifactNotFoundException {
		DownloadStatusImpl status = new DownloadStatusImpl(0);
		status.setFinished();
		return status;
	}

	public ClassLoader getLoader(Artifact a, ClassLoader parent)
			throws ArtifactNotFoundException, ArtifactStateException {
		ClassLoader cl = getClass().getClassLoader();
		if (cl != null) {
			return cl;
		}
		return ClassLoader.getSystemClassLoader();
	}

	public ArtifactStatus getStatus(Artifact a) {
		return ArtifactStatus.Ready;
	}

	public void removeRepositoryListener(RepositoryListener l) {

	}

	public void update() {

	}

}
