package net.sf.taverna.service.rest;

import net.sf.taverna.service.interfaces.TavernaService;
import net.sf.taverna.service.test.TestCommon;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.restlet.data.MediaType;

/**
 * Base class for unit tests that need a running server. The class will start
 * the server on initialization and shut it down afterwards.
 * 
 * @author Stian Soiland
 */
public abstract class ClientTest extends TestCommon {

	public static final int PORT = 8977;

	public static final String BASE_URL = "http://localhost:" + PORT;
	
	public static final MediaType restType = new MediaType(TavernaService.restType);

	public static final MediaType scuflType = new MediaType(TavernaService.scuflType);

	public static final MediaType baclavaType = new MediaType(TavernaService.baclavaType);

	private static RestApplication server = new RestApplication();
	
	@BeforeClass
	public static void startServer() throws Exception {
		stopServer();
		server = new RestApplication();
		server.startServer(PORT);
	}

	@AfterClass
	public static void stopServer() throws Exception {
		server.stopServer();
	}
	
}
