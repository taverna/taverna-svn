/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages classloaders for the various plugin systems to ensure class isolation
 * (and also to remove the need to specify every class on the command line which
 * was getting a bit tedious)
 * 
 * @author Tom Oinn
 */
public class PluginManager {

	static ClassLoader root;

	static ClassLoader core = null;

	static ClassLoader common = null;

	static List plugins = new ArrayList();

	public static void init() {
		root = PluginManager.class.getClassLoader();
	}

	public static void init(File home) {
		init();
		try {
			File coreFile = new File(home, "lib/core/");
			File commonFile = new File(home, "lib/common/");
			if (coreFile == null || commonFile == null) {
				System.out
						.println("Unable to locate core and common locations, aborting.");
				return;
			}
			core = createLoader(coreFile, root);
			common = createLoader(commonFile, core);
			File pluginRoot = new File(home, "lib/plugins/");
			if (pluginRoot == null) {
				System.out
						.println("Unable to locate plugin location, aborting.");
				return;
			}
			File[] pluginDirs = pluginRoot.listFiles(new FilenameFilter() {
				public boolean accept(File file, String str) {
					return (file.isDirectory());
				}
			});
			if (pluginDirs == null) {
				System.out.println("No plugin directories, aborting.");
				return;
			}
			for (int i = 0; i < pluginDirs.length; i++) {
				plugins.add(createLoader(pluginDirs[i], common));
			}
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		}
	}

	private static ClassLoader createLoader(File file, ClassLoader parent)
			throws MalformedURLException {
		System.out.println("Defining classpath for file : " + file.toString());
		// Traverse the file and add any jar files found within to the loader
		File[] jars = file.listFiles(new FilenameFilter() {
			public boolean accept(File file, String str) {
				return (str.endsWith(".jar"));
			}
		});
		if (jars == null) {
			return new URLClassLoader(new URL[0]);
		}
		URL[] urls = new URL[jars.length + 1];
		urls[0] = file.toURL();
		for (int i = 0; i < jars.length; i++) {
			System.out.println("  " + jars[i].toString());
			urls[i + 1] = jars[i].toURL();
		}
		return new URLClassLoader(urls, parent);
	}

	public static ClassLoader[] getPluginClassLoaders() {
		return (ClassLoader[]) plugins.toArray(new ClassLoader[0]);
	}

}
