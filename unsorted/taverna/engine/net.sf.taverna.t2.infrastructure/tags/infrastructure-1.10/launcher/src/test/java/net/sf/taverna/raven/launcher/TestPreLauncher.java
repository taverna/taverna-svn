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
package net.sf.taverna.raven.launcher;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import net.sf.taverna.raven.appconfig.AbstractPropThreadTest;
import net.sf.taverna.raven.prelauncher.PreLauncher;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class TestPreLauncher extends AbstractPropThreadTest {

	@Test
	public void launchHelloWorld() throws Exception {
		System.setProperty("raven.launcher.app.name", "helloworld");
		System.setProperty("raven.launcher.app.main",
				"net.sf.taverna.raven.helloworld.HelloWorld");
		List<URL> urls = makeClassPath("TestPreLauncher-helloworld/");
		URLClassLoader classLoader = new URLClassLoader(urls
				.toArray(new URL[0]), getClass().getClassLoader());
		Thread.currentThread().setContextClassLoader(classLoader);
		classLoader.loadClass("org.apache.log4j.Logger");

		File outFile = File.createTempFile(getClass().getCanonicalName(),
				"test");
		outFile.deleteOnExit();

		PreLauncher.main(new String[] { outFile.getAbsolutePath() });
		String out = FileUtils.readFileToString(outFile, "utf8");
		assertEquals("Did not match expected output",
				"This is the test data.\n", out);
	}
}
