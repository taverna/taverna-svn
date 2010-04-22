package net.sf.taverna.t2.provenance.query;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;

/**
 * Test a workflow in which a processor has an output port called the same as an input port.
 * Test for bug T2-1051
 * 
 * @author Stian Soiland-Reyes
 * 
 */
@Ignore("Disabled while doing T2-1308")
public class ProcessorPortSameNameQueryTest extends AbstractQueryTestHelper {

	protected Map<String, Object> getWorkflowInputs() {
		Map<String, Object> inputs = new HashMap<String, Object>();		
		return inputs;
	}

	protected Map<String, Object> getExpectedWorkflowOutputs() {
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("output[]", "output");
		return inputs;
	}

	protected String getWorkflowName() {
		return "t2-1051";
	}

	@Override
	protected Map<String, Object> getExpectedIntermediateValues() {
		Map<String, Object> expectedIntermediateValues = new HashMap<String, Object>();

		String df0 = dataflow.getInternalIdentier() + "/";

		expectedIntermediateValues.put(df0 + "input/value[]", "input");
		expectedIntermediateValues.put(df0 + "T2_1051/i:parameter[]", "input");
		expectedIntermediateValues.put(df0 + "T2_1051/o:parameter[]", "output");

		return expectedIntermediateValues;
	}

	@Override
	protected Map<String, Object> getExpectedCollections() {
		return new HashMap<String, Object>();
	}


}
