package net.sf.taverna.t2.activities.sadi.views;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.sf.taverna.t2.activities.sadi.SADIActivity;
import net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean;
import net.sf.taverna.t2.activities.sadi.SADIUtils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.wilkinsonlab.sadi.client.Registry;
import ca.wilkinsonlab.sadi.client.RegistryImpl;
import ca.wilkinsonlab.sadi.client.Service;
import ca.wilkinsonlab.sadi.rdfpath.RDFPath;
import ca.wilkinsonlab.sadi.utils.QueryExecutorFactory;

import com.hp.hpl.jena.util.LocationMapper;

/**
 * @author Luke McCarthy
 */
public class SADIConfigurationPanelTest
{
	private static final String SERVICE_URI = "http://sadiframework.org/examples/calculateBMI";
	private static Service service;
	private static SADIActivity activity;
	private SADIActivityConfigurationBean configurationBean;
	private SADIConfigurationPanel panel;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		LocationMapper.get().addAltPrefix(
				"http://sadiframework.org/examples/", 
				"file:src/test/resources/");
		LocationMapper.get().addAltPrefix(
				"http://sadiframework.org/ontologies/",
				"file:src/test/resources/");
		
		Registry registry = new RegistryImpl(QueryExecutorFactory.createFileModelQueryExecutor("src/test/resources/registry.rdf"));
		service = registry.getService(SERVICE_URI);
		activity = new SADIActivity() {
			@Override
			public Service getService() {
				return service;
			}
		};
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		LocationMapper.get().removeAltPrefix("http://sadiframework.org/examples/");
		LocationMapper.get().removeAltPrefix("http://sadiframework.org/ontologies/");
		service = null;
		activity = null;
	}

	@Before
	public void setUp() throws Exception
	{
		configurationBean = new SADIActivityConfigurationBean();
		configurationBean.setSparqlEndpoint("http://biordf.net/sparql");
		configurationBean.setGraphName("http://sadiframework.org/registry/");
		configurationBean.setServiceURI(SERVICE_URI);
		activity.configure(configurationBean);
		panel = new SADIConfigurationPanel(activity);
	}

	@After
	public void tearDown() throws Exception
	{
		configurationBean = null;
		panel = null;
	}
	
	@Test
	public void testDefaultConfigurationInput()
	{
		assertFalse(configurationBean.getInputPortMap().isEmpty());
		assertCollectionsAreEquivalent(configurationBean.getInputPortMap().values(), 
				panel.getConfiguration().getInputPortMap().values());
		assertCollectionsAreEquivalent(configurationBean.getInputPortMap().values(), 
				convertPaths(panel.sadiInputTreeModel.getSelectedPaths()));
	}
	
	@Test
	public void testDefaultConfigurationOutput()
	{
		assertFalse(configurationBean.getOutputPortMap().isEmpty());
		assertCollectionsAreEquivalent(configurationBean.getOutputPortMap().values(), 
				panel.getConfiguration().getOutputPortMap().values());
		assertCollectionsAreEquivalent(configurationBean.getOutputPortMap().values(), 
				convertPaths(panel.sadiOutputTreeModel.getSelectedPaths()));
	}

	@Test
	public void testIsConfigurationChangedInput()
	{
		assertFalse(panel.getConfiguration().getInputPortMap().isEmpty());
		panel.sadiInputTreeModel.clearSelectedPaths();
		assertTrue(panel.getConfiguration().getInputPortMap().isEmpty());
		assertTrue(panel.isConfigurationChanged());
	}

	@Test
	public void testIsConfigurationChangedOutput()
	{
		assertFalse(panel.getConfiguration().getOutputPortMap().isEmpty());
		panel.sadiOutputTreeModel.clearSelectedPaths();
		assertTrue(panel.getConfiguration().getOutputPortMap().isEmpty());
		assertTrue(panel.isConfigurationChanged());
	}

	@Test
	public void testGetConfigurationInput()
	{
		Collection<RDFPath> paths = Arrays.asList(new RDFPath[] {
				new RDFPath("http://mged.sourceforge.net/ontologies/MGEDOntology.owl#has_mass some http://mged.sourceforge.net/ontologies/MGEDOntology.owl#Measurement, http://mged.sourceforge.net/ontologies/MGEDOntology.owl#has_value some http://www.w3.org/2001/XMLSchema#string"),
				new RDFPath("http://mged.sourceforge.net/ontologies/MGEDOntology.owl#has_mass some http://mged.sourceforge.net/ontologies/MGEDOntology.owl#Measurement, http://mged.sourceforge.net/ontologies/MGEDOntology.owl#has_units some http://mged.sourceforge.net/ontologies/MGEDOntology.owl#Unit"),
				new RDFPath("http://mged.sourceforge.net/ontologies/MGEDOntology.owl#has_height some http://mged.sourceforge.net/ontologies/MGEDOntology.owl#Measurement, http://mged.sourceforge.net/ontologies/MGEDOntology.owl#has_value some http://www.w3.org/2001/XMLSchema#string"),
				new RDFPath("http://mged.sourceforge.net/ontologies/MGEDOntology.owl#has_height some http://mged.sourceforge.net/ontologies/MGEDOntology.owl#Measurement, http://mged.sourceforge.net/ontologies/MGEDOntology.owl#has_units some http://mged.sourceforge.net/ontologies/MGEDOntology.owl#Unit")});
		panel.sadiInputTreeModel.clearSelectedPaths();
		panel.sadiInputTreeModel.selectPaths(paths);
		assertCollectionsAreEquivalent(paths,
				SADIUtils.convertPaths(panel.getConfiguration().getInputPortMap().values()));
	}

	@Test
	public void testGetConfigurationOutput()
	{
		Collection<RDFPath> paths = Collections.singleton(
				new RDFPath("http://sadiframework.org/examples/bmi.owl#BMI"));
		panel.sadiOutputTreeModel.clearSelectedPaths();
		panel.sadiOutputTreeModel.selectPaths(paths);
		assertCollectionsAreEquivalent(paths,
				SADIUtils.convertPaths(panel.getConfiguration().getOutputPortMap().values()));
	}
	
	/**
	 * Passes if every element in one collection is equal to an element in
	 * a second collection, irrespective of order.
	 * @param col1
	 * @param col2
	 */
	private static void assertCollectionsAreEquivalent(Collection<?> col1, Collection<?> col2)
	{
		assertTrue(col1.containsAll(col2));
		assertTrue(col2.containsAll(col1));
	}
	
	private static Collection<String> convertPaths(Collection<RDFPath> paths)
	{
		Collection<String> pathSpecs = new ArrayList<String>(paths.size());
		for (RDFPath path: paths)
			pathSpecs.add(path.toString());
		return pathSpecs;
	}
}
