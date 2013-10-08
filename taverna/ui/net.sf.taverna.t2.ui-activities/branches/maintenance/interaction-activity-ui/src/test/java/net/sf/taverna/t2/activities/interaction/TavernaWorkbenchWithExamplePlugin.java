package net.sf.taverna.t2.activities.interaction;

import java.net.URL;

import net.sf.taverna.raven.launcher.Launcher;

/**
 * Run with parameters:
 * 
 * -Xmx300m -XX:MaxPermSize=140m -Dsun.swing.enableImprovedDragGesture
 * -Dtaverna.startup=.
 * 
 * NOTE: Do not save any workflows made using this test mode, as the plugin
 * information will be missing from the workflow file, and it will not open in a
 * Taverna run normally.
 * 
 */
public class TavernaWorkbenchWithExamplePlugin {
	public static void main(final String[] args) throws Exception {
		final URL dir = TavernaCommandLineWithExamplePlugin.class
				.getResource("/conf/current-profile.xml");
		// System.setProperty("raven.launcher.app.main",
		// "net.sf.taverna.t2.commandline.CommandLineLauncher");
		System.setProperty("taverna.startup", dir.toURI().resolve("../")
				.getPath());
		System.setProperty("raven.launcher.app.name", "taverna-2.4.0SNAPSHOT");
		System.setProperty("raven.launcher.app.title",
				"Taverna Workbench 2.4.0-SNAPSHOT");
		Launcher.main(args);
	}
}
