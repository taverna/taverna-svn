package uk.org.mygrid.logbook.boca;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.org.mygrid.provenance.util.TestUtils;

public class BocaImporterTest {

	private static BocaImporter bocaImporter;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestUtils.setTestProperties();
		ClassLoader classLoader = BocaImporterTest.class.getClassLoader();
		Properties legacyConfiguration = new Properties();
		legacyConfiguration.load(classLoader
				.getResourceAsStream("legacy.properties"));
		Properties bocaConfiguration = new Properties();
		bocaConfiguration.load(classLoader
				.getResourceAsStream("bocaMysqlTest.properties"));
		bocaImporter = new BocaImporter(legacyConfiguration, bocaConfiguration);
		bocaImporter.getBocaService().clear();
	}

	@Test
	public void testTransfer() throws Exception {
		MetadataService legacyService = bocaImporter.getLegacyService();
		Set<String> legacyWorkflowRuns = new HashSet<String>(legacyService.getAllWorkflowRuns());
		int legacySize = legacyWorkflowRuns.size();
		System.out.println("Legacy workflowRuns = "+ legacySize);
		bocaImporter.transfer();
		MetadataService bocaService = bocaImporter.getBocaService();
		List<String> allWorkflowRuns = bocaService.getAllWorkflowRuns();
		int bocaSize = allWorkflowRuns.size();
		System.out.println("Boca workflowRuns = "+ bocaSize);
		assertEquals(legacySize, bocaSize);
	}

}
