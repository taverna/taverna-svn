package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ParallelizeConfig;

import org.jdom.Element;
import org.junit.Test;


public class DispatchLayerXMLDeserializerTest extends DeserializerTestsHelper {
	DispatchLayerXMLDeserializer deserializer = DispatchLayerXMLDeserializer.getInstance();
	
	@Test
	public void testDispatchLayer() throws Exception {
		Element el = loadXMLFragment("dispatchLayer.xml");
		DispatchLayer<?> layer = deserializer.deserializeDispatchLayer(el);
		assertTrue("Should be a Parallelize layer",layer instanceof Parallelize);
		Parallelize para = (Parallelize)layer;
		assertTrue("config should be ParellizeConfig",para.getConfiguration() instanceof ParallelizeConfig);
		assertEquals("max jobs should be 7",7,((ParallelizeConfig)para.getConfiguration()).getMaximumJobs());
	}
}
