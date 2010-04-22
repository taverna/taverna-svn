package net.sf.taverna.t2.provenance.database;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test a workflow which has an output port called the same as an input port
 * Related to bug T2-1051
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class WorkflowPortSameNameDbTest extends AbstractDbTestHelper {

	protected Map<String, Object> getWorkflowInputs() {
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("duplicate", "abcd");
		return inputs;
	}

	protected Map<String, Object> getExpectedWorkflowOutputs() {
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("duplicate[]", "abcdabcd");
		return inputs;
	}

	protected String getWorkflowName() {
		return "duplicate-input-output-port";
	}

	@Override
	protected Map<String, Object> getExpectedIntermediateValues() {
		Map<String, Object> expectedIntermediateValues = new HashMap<String, Object>();
		String df0 = dataflow.getInternalIdentier() + "/";
		expectedIntermediateValues.put(df0 + "Concatenate_two_strings/i:string1[]", "abcd");
		expectedIntermediateValues.put(df0 + "Concatenate_two_strings/i:string2[]", "abcd");
		expectedIntermediateValues.put(df0 + "Concatenate_two_strings/o:output[]", "abcdabcd");

		return expectedIntermediateValues;
	}

	@Override
	protected Map<String, Object> getExpectedCollections() {
		return new HashMap<String, Object>();
	}


}
