package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategy;

import org.jdom.Element;
import org.junit.Test;

public class IterationStragegyStackXMLDeserializerTest extends DeserializerTestsHelper {
	private IterationStrategyStackXMLDeserializer deserializer = IterationStrategyStackXMLDeserializer.getInstance();
	
	@Test
	public void testCrossProducts() throws Exception {
		Element el = loadXMLFragment("2_port_cross_product.xml");
		Processor p =edits.createProcessor("test");
		
		deserializer.deserializeIterationStrategyStack(el, p.getIterationStrategy());
		assertEquals("There should be 1 strategy",1,p.getIterationStrategy().getStrategies().size());
		
		IterationStrategy strat = p.getIterationStrategy().getStrategies().get(0);
		
		assertNotNull(strat.getDesiredCardinalities());
		assertNotNull(strat.getDesiredCardinalities().get("nested_beanshell_in"));
		assertNotNull(strat.getDesiredCardinalities().get("nested_beanshell_in2"));
		
		assertEquals("cardinality should be 0",Integer.valueOf(0),strat.getDesiredCardinalities().get("nested_beanshell_in"));
		assertEquals("cardinality should be 1",Integer.valueOf(1),strat.getDesiredCardinalities().get("nested_beanshell_in2"));
		
	}
}
