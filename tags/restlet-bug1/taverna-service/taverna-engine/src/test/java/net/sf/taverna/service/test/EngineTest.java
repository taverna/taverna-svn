package net.sf.taverna.service.test;

import java.net.MalformedURLException;

import net.sf.taverna.service.backend.Engine;
import net.sf.taverna.tools.Bootstrap;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;

public class EngineTest extends TestCommon {
	private static Logger logger = Logger.getLogger(EngineTest.class);
	
	// Note: Engine.prepare() creates a temporary taverna home for us
	
//
//	private static File tavernaHome;

//	/**
//	 * Create a temporary taverna.home, fresh and empty.
//	 */
//	@BeforeClass
//	public synchronized static void makeTempTavernaHome() {
//		try {
//			tavernaHome = File.createTempFile("taverna", "home");
//			tavernaHome.delete();
//			tavernaHome.mkdir();
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new NullPointerException("Can't make taverna.home");
//		}
//		System.setProperty("taverna.home", tavernaHome.getAbsolutePath());
//		Bootstrap.findUserDir();
//		Bootstrap.properties = Bootstrap.findProperties();
//		Bootstrap.remoteRepositories =
//			Bootstrap.findRepositories(Bootstrap.properties);
//		if (Bootstrap.properties.getProperty("raven.remoteprofile") != null) {
//			Bootstrap.initialiseProfile(Bootstrap.properties.getProperty("raven.remoteprofile"));
//		}
//		File logDir = new File(tavernaHome, "logs");
//		System.setProperty("taverna.logdir", logDir.getAbsolutePath());
//	}

	@BeforeClass
	public synchronized static void prepareBootstrap()
		throws MalformedURLException, ClassNotFoundException,
		NoSuchMethodException, IllegalAccessException {
//		if (tavernaHome == null) {
//			makeTempTavernaHome();
//			assertNotNull("Did not create temporary taverna.home", tavernaHome);
//		}
		if (Bootstrap.properties == null) {
			Engine.bootstrap();
			Engine.init();
		}
	}

//	/**
//	 * Remove that temporary taverna.home
//	 * 
//	 * @throws IOException
//	 */
//	@AfterClass
//	public static void deleteTavernaHome() throws IOException {
//		try {
//			if (tavernaHome == null) {
//				return;
//			}
//			FileUtils.deleteDirectory(tavernaHome);
//		} finally {
//			System.clearProperty("taverna.home");
//		}
//	}

}
