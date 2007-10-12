package net.sf.taverna.t2.activities.stringconstant.translator;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.workflowmodel.AbstractPort;

import org.embl.ebi.escience.scuflworkers.stringconstant.StringConstantProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the translation of a T1 StringConstantProcessor to a T2 StringConstantActivity
 * @author Stuart Owen
 *
 */
public class StringConstantActivityTranslatorTest {

	@Test
	public void testTranslation() throws Exception {
		StringConstantProcessor processor = new StringConstantProcessor(null,
				"simplestringconstant", "the_string_value");
		StringConstantActivity activity = (StringConstantActivity)new StringConstantActivityTranslator().doTranslation(processor);
		assertEquals("there should be no inputs",0,activity.getInputPorts().size());
		assertEquals("there should be 1 output",1,activity.getOutputPorts().size());
		assertEquals("the output port name should be value","value",((AbstractPort)activity.getOutputPorts().toArray()[0]).getName());
		assertEquals("the value should be 'the_string_value'","the_string_value",activity.getStringValue());
	}
}
