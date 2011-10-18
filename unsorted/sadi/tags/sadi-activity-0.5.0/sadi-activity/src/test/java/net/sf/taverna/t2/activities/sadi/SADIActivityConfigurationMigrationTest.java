package net.sf.taverna.t2.activities.sadi;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.wilkinsonlab.sadi.rdfpath.RDFPath;
import ca.wilkinsonlab.sadi.utils.OnymizeUtils;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class SADIActivityConfigurationMigrationTest
{
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

//	@Test
//	public void testUpdateConfiguration03xTo04x() throws Exception
//	{
//		String xml = SPARQLStringUtils.readFully(SADIActivityConfigurationMigrationTest.class.getResourceAsStream("/v0.3.7.t2flow"));
//		SADIActivityConfigurationBean config = (SADIActivityConfigurationBean)(new XStream().fromXML(xml));
//		log.debug(config.getInputRestrictionPaths());
//	}

	@Test
	public void testConvert03xStringPathToRDFPath() throws Exception
	{
		List<String> oldPath = Arrays.asList(new String[] {"InputClass", 
				"has_mass (Measurement)", "has_units (Unit)" });
		RDFPath newPath = SADIActivityConfigurationMigration.convert03xStringPathToRDFPath(oldPath);
		RDFPath expectedPath = new RDFPath("urn:label:has_mass some urn:label:Measurement, urn:label:has_units some urn:label:Unit");
		assertEquals(expectedPath, newPath);
		
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		ontModel.read(SADIActivityConfigurationMigration.class.getResourceAsStream("/MGED.measurement.owl"), null);
		RDFPath deonymizedPath = OnymizeUtils.deonymizePath(ontModel, newPath, "UTF-8");
		expectedPath = new RDFPath(
				ontModel.getProperty("http://mged.sourceforge.net/ontologies/MGEDOntology.owl#has_mass"),
				ontModel.getProperty("http://mged.sourceforge.net/ontologies/MGEDOntology.owl#Measurement"),
				ontModel.getProperty("http://mged.sourceforge.net/ontologies/MGEDOntology.owl#has_units"),
				ontModel.getProperty("http://mged.sourceforge.net/ontologies/MGEDOntology.owl#Unit"));
		assertEquals(expectedPath, deonymizedPath);
	}

	@Test
	public void testConvert03xPathListTo04xPathMap() throws Exception
	{
		List<String> oldPath = Arrays.asList(new String[] {"InputClass", 
				"has_mass (Measurement)", "has_units (Unit)" });
		Map<String, String> newPathMap = new HashMap<String, String>();
		SADIActivityConfigurationMigration.convert03xPathListTo04xPathMap(Collections.singletonList(oldPath), newPathMap, "port");
		assertEquals("urn:label:has_mass some urn:label:Measurement, urn:label:has_units some urn:label:Unit",
				newPathMap.values().iterator().next());
	}
}
