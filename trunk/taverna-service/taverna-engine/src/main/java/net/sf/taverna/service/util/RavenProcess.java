package net.sf.taverna.service.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

public class RavenProcess extends JavaProcess {

	private static Logger logger = Logger.getLogger(RavenProcess.class);

	public static final String RAVEN_CLASS = "net.sf.taverna.tools.Bootstrap";

	private static final String RAVEN_CLASS_PATH =
		"/net/sf/taverna/tools/Bootstrap.class";

	private static final String RAVEN_PARENT = "../../../..";

	private String groupId;

	private String artifactId;

	private String version;

	public RavenProcess(String groupId, String artifactId, String version,
		String className, String method) {
		super(findRaven().toString(), RAVEN_CLASS);
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		
		addSystemProperty("raven.target.groupid", groupId);
		addSystemProperty("raven.target.artifactid", artifactId);
		addSystemProperty("raven.target.version", version);
		addSystemProperty("raven.target.class", className);
		addSystemProperty("raven.target.method", method);
	}

	public static URL findRaven() {
		URL ravenURL = RavenProcess.class.getResource(RAVEN_CLASS_PATH);
		if (ravenURL == null) {
			logger.error("Could not find Raven in classpath");
			return null;
		}
		try {
			if (ravenURL.getProtocol().equals("jar")) {
				return new URL(ravenURL.getPath().split("!")[0]);
			} else {
				return new URL(ravenURL, RAVEN_PARENT);
			}
		} catch (MalformedURLException e) {
			logger.warn("Could not create URL for Raven", e);
			return null;
		}
	}

}
