package net.sf.taverna.t2.cloudone.p2p.http;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractCloudOneServerTest {

	public static final String HOST = "localhost";
	public static final int PORT = 7381;
	protected static CloudOneApplication cloudApp;

	public AbstractCloudOneServerTest() {
		super();
	}

	@BeforeClass
	public static void startServer() {
		cloudApp = new CloudOneApplication(HOST, PORT);
		cloudApp.startServer();
	}

	@AfterClass
	public static void stopServer() {
		cloudApp.stopServer();
	}

}