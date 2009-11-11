package net.sf.taverna.raven.plugins;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.x2008.xml.plugins.DescribedPlugin;
import net.sf.taverna.x2008.xml.plugins.PluginDocument;
import net.sf.taverna.x2008.xml.plugins.PluginsDocument;

import org.apache.xmlbeans.XmlOptions;
import org.junit.Before;
import org.junit.Test;

public class TestPluginLoadFromXML {

	private URL pluginsXML;
	private URL missingNamespaceXML;
	private URL minimalPlugins;
	private URL mixedDependencies;

	@Before
	public void findXMLs() {
		String name = "plugins.xml";
		pluginsXML = getClass().getResource(name);
		assertNotNull("Could not find " + name, pluginsXML);

		name = "missingNamespace.xml";
		missingNamespaceXML = getClass().getResource(name);
		assertNotNull("Could not find " + name, missingNamespaceXML);

		name = "minimalPlugins.xml";
		minimalPlugins = getClass().getResource(name);
		assertNotNull("Could not find " + name, minimalPlugins);
		
		name = "mixedDependencies.xml";
		mixedDependencies = getClass().getResource(name);
		assertNotNull("Could not find " + name, mixedDependencies);
	}

	@Test
	public void loadPluginsXML() throws Exception {
		Plugin plugin;
		PluginsDocument pluginDoc = PluginsDocument.Factory.parse(pluginsXML,
				makeXMLOptions());
		List<Plugin> plugins = new ArrayList<Plugin>();
		for (DescribedPlugin pluginDescr : pluginDoc.getPlugins()
				.getPluginArray()) {
			plugin = Plugin.fromXmlBean(pluginDescr);
			assertTrue(plugin.isEnabled());

			plugins.add(plugin);
		}
		assertEquals(4, plugins.size());
		assertEquals("net.sf.taverna.t2.workbench.ui.imp", plugins.get(0)
				.getIdentifier());
		assertEquals("net.sf.taverna.t2.all-activities", plugins.get(1)
				.getIdentifier());
		assertEquals("net.sf.taverna.t2.all-activity-translators", plugins.get(
				2).getIdentifier());
		assertEquals("net.sf.taverna.t2.credential-manager", plugins.get(3)
				.getIdentifier());

	}

	@Test
	public void loadMissingNamespaceXML() throws Exception {
		Plugin plugin;
		PluginsDocument pluginDoc = PluginsDocument.Factory.parse(
				missingNamespaceXML, makeXMLOptions());
		List<Plugin> plugins = new ArrayList<Plugin>();
		for (DescribedPlugin pluginDescr : pluginDoc.getPlugins()
				.getPluginArray()) {
			plugin = Plugin.fromXmlBean(pluginDescr);
			assertTrue(plugin.isEnabled());
			plugins.add(plugin);
		}
		assertEquals(4, plugins.size());
		assertEquals("net.sf.taverna.t2.workbench.ui.imp", plugins.get(0)
				.getIdentifier());
		assertEquals("net.sf.taverna.t2.all-activities", plugins.get(1)
				.getIdentifier());
		assertEquals("net.sf.taverna.t2.all-activity-translators", plugins.get(
				2).getIdentifier());
		assertEquals("net.sf.taverna.t2.credential-manager", plugins.get(3)
				.getIdentifier());
	}

	@Test
	public void loadMixedDependencies() throws Exception {
		Plugin plugin;
		PluginsDocument pluginDoc = PluginsDocument.Factory.parse(
				mixedDependencies, makeXMLOptions());
		plugin = Plugin.fromXmlBean(pluginDoc.getPlugins().getPluginArray(0));
		assertTrue("Plugin was not enabled " + plugin, plugin.isEnabled());
		
		Set<Artifact> artifacts = plugin.getProfile().getArtifacts();
		BasicArtifact fileImpl = new BasicArtifact(
				"net.sf.taverna.t2.workbench", "file-impl", "0.1-SNAPSHOT");
		BasicArtifact uiImpl = new BasicArtifact(
				"net.sf.taverna.t2.workbench", "ui-impl", "0.1-SNAPSHOT");
		BasicArtifact saxpath = new BasicArtifact(
				"saxpath", "saxpath", "1.0-FCS");
		BasicArtifact jaxen = new BasicArtifact(
				"jaxen", "jaxen", "1.0-FCS");
		
		Set<BasicArtifact> expected = new HashSet<BasicArtifact>(Arrays.asList(fileImpl, uiImpl, saxpath, jaxen));
		assertEquals("Profile did not contain expected artifacts " + expected, expected,
				artifacts);

		Set<BasicArtifact> expectedSystem = new HashSet<BasicArtifact>(Arrays.asList(saxpath, jaxen));
		assertEquals("Profile did not contain expected system artifacts " + expectedSystem, expectedSystem,
				plugin.getProfile().getSystemArtifacts());
	}
	
	@Test
	public void loadMinimalPlugins() throws Exception {
		Plugin plugin;
		PluginsDocument pluginDoc = PluginsDocument.Factory.parse(
				minimalPlugins, makeXMLOptions());
		plugin = Plugin.fromXmlBean(pluginDoc.getPlugins().getPluginArray(0));
		assertEquals("taverna.sf.net", plugin.getProvider());
		assertEquals("net.sf.taverna.t2.workbench.ui.imp", plugin
				.getIdentifier());
		assertEquals("0.3-SNAPSHOT", plugin.getVersion());
		assertEquals("workbench", plugin.getName());
		assertEquals(null, plugin.getDescription());
		assertTrue(plugin.isEnabled());
		assertEquals(Collections.EMPTY_LIST, plugin.getRepositories());
		Set<Artifact> artifacts = plugin.getProfile().getArtifacts();
		assertEquals(1, artifacts.size());
		BasicArtifact fileImpl = new BasicArtifact(
				"net.sf.taverna.t2.workbench", "file-impl", "0.1-SNAPSHOT");
		assertTrue("Profile did not contain artifact " + fileImpl, artifacts
				.contains(fileImpl));
		assertEquals(Collections.singletonList("2.0"), plugin.getVersions());

	}

	private XmlOptions makeXMLOptions() {
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSavePrettyPrintIndent(4);
		xmlOptions.setLoadStripWhitespace();
		xmlOptions.setLoadReplaceDocumentElement(new QName(
				"http://taverna.sf.net/2008/xml/plugins", "plugins"));
		return xmlOptions;
	}
}
