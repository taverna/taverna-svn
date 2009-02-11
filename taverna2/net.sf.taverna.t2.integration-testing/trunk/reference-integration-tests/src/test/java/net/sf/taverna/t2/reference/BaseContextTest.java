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
package net.sf.taverna.t2.reference;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.RepositoryListener;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Trivial test to check whether there was an obscure problem with the
 * t2reference-impl loading through raven. Apparently there is a problem to do
 * with avoiding parsing out parents when not required, it's not critical but
 * I'll keep this here for now as a reminder.
 * 
 * @author Tom Oinn
 */
public class BaseContextTest {

	@Test
	public void doTest() {

		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"baseContextTestContext.xml");

		Repository rep = (Repository) context.getBean("raven.repository");

		Artifact a = new BasicArtifact("net.sf.taverna.t2", "t2reference-impl",
				"0.3-SNAPSHOT");

		rep.addRepositoryListener(new RepositoryListener() {

			public void statusChanged(Artifact arg0, ArtifactStatus arg1,
					ArtifactStatus arg2) {
				System.out.println(arg0.toString() + ":" + arg1.toString()
						+ "->" + arg2.toString());
			}

		});

		rep.addArtifact(a);

		System.out.println(rep.getStatus(a));

		rep.update();

		System.out.println(rep.getStatus(a));

	}

}
