package net.sf.taverna.t2.component.registry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientVersion {
	private static final String DEFAULT_VERSION = "1.1.0";
	public static final String VERSION;

	private ClientVersion() {
	}

	static {
		InputStream is = ClientVersion.class
				.getResourceAsStream("version.properties");
		String version = DEFAULT_VERSION;
		if (is != null)
			try {
				Properties p = new Properties();
				p.load(is);
				version = p.getProperty("project.version", DEFAULT_VERSION);
			} catch (IOException e) {
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		VERSION = version;
	}

}
