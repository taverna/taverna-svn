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
package net.sf.taverna.raven;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.RepositoryListener;

/**
 * Applicaton to run a normal Java application from a classloader acquired by
 * Raven.
 * 
 * @author Matthew Pocock
 */
public class Raven {
	public static void failure() {
		System.out
				.println("Use exactly the following command-line, with options in exactly "
						+ "this order:\n"
						+ "localRepository remoteRepositoryURL1..n groupId artifactId"
						+ " version applicationClassName debug [options-to-pass-through]\n"
						+ "where remoteRepositoryX is a URL containing a protocol and"
						+ " debug is true/false");
		System.exit(-1);
	}

	public static void main(String[] args) throws Throwable {
		LinkedList<String> argL = new LinkedList<String>(Arrays.asList(args));

		File localRepository;
		if (argL.isEmpty())
			failure();
		localRepository = new File(argL.removeFirst());

		List<URL> repos = new ArrayList<URL>();
		if (argL.isEmpty())
			failure();
		while (argL.getFirst().matches("\\w+://.*")) {
			repos.add(new URL(argL.removeFirst()));
		}
		if (repos.isEmpty())
			failure();

		String groupId;
		if (argL.isEmpty())
			failure();
		groupId = argL.removeFirst();

		String artifactId;
		if (argL.isEmpty())
			failure();
		artifactId = argL.removeFirst();

		String version;
		if (argL.isEmpty())
			failure();
		version = argL.removeFirst();

		String appClassName;
		if (argL.isEmpty())
			failure();
		appClassName = argL.removeFirst();

		boolean debug;
		if (argL.isEmpty())
			failure();
		debug = Boolean.valueOf(argL.removeFirst());

		RepositoryListener listener;

		if (debug) {
			listener = new RepositoryListener() {
				public void statusChanged(Artifact artifact,
						ArtifactStatus oldStatus, ArtifactStatus newStatus) {
					System.err.println("Artifact " + artifact
							+ " has changed state from " + oldStatus + " to "
							+ newStatus);
				}
			};
		} else {
			listener = new RepositoryListener() {
				public void statusChanged(Artifact artifact,
						ArtifactStatus oldStatus, ArtifactStatus newStatus) {
					// noop
				}
			};
		}

		Class<?> appClass = Loader.doRavenMagic(localRepository, repos
				.toArray(new URL[] {}), groupId, artifactId, version,
				appClassName, listener);

		Method main = appClass.getMethod("main", String[].class);
		main.invoke(null, (Object[])argL.toArray(new String[] {}));
	}
}
