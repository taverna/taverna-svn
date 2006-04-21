/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;

/**
 * Bootstrap launcher to avoid having to set an insanely long classpath.
 * 
 * @author Tom Oinn
 */
public class WorkbenchLauncher {

	public static ClassLoader LOADER = null;

	public static void main(String[] args) throws Exception {
		if (System.getProperty("taverna.home") == null) {
			System.out.println("Can't find taverna home directory, failing.");
			System.exit(100);
		}
		File home = new File(System.getProperty("taverna.home"));
		List jarURLList = new ArrayList();
		addJars(new File(home, "lib/"), jarURLList);
		addJars(new File(home, "plugins/"), jarURLList);
		addJars(new File(home, "conf/"), jarURLList);
		addJars(new File(home, "resources/"), jarURLList);
		URL[] urls = (URL[]) jarURLList.toArray(new URL[0]);
		URLClassLoader loader = new URLClassLoader(urls,
				WorkbenchLauncher.class.getClassLoader());
		LOADER = loader;
		Class workbenchClass = loader
				.loadClass("org.embl.ebi.escience.scuflui.workbench.Workbench");
		Method mainMethod = workbenchClass.getDeclaredMethod("main",
				new Class[] { String[].class });
		Thread.currentThread().setContextClassLoader(loader);
		Class xercesClass = loader
				.loadClass("org.apache.xerces.parsers.SAXParser");
		mainMethod.invoke(null, new Object[] { args });
	}

	static void addJars(File dir, List populate) throws Exception {
		if (dir.exists()) {
			File[] jars = dir.listFiles(new FilenameFilter() {
				public boolean accept(File file, String str) {
					return (str.endsWith(".jar"));
				}
			});
			populate.add(dir.toURL());
			if (jars != null) {
				for (int i = 0; i < jars.length; i++) {
					populate.add(jars[i].toURL());
				}
			}
		}
	}

}
