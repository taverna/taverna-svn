package net.sf.taverna.t2.workbench.dev;

import java.net.URISyntaxException;
import java.net.URL;

import net.sf.taverna.raven.launcher.Launcher;


public class DeveloperWorkbench {

	/**
	 * Remember running with VM parameters:
	 * 
	 * -Xmx400m -XX:MaxPermSize=140m
	 * 
	 */
	public static void main(String[] args) throws URISyntaxException {
		URL dir = DeveloperWorkbench.class.getResource("/conf/current-profile.xml");
		System.setProperty("taverna.startup", dir.toURI().resolve("../").getPath());
		Launcher.main(args);
	}

}
