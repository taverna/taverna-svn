package net.sf.taverna.t2.provenance.query;

import java.util.HashMap;
import java.util.Map;

/**
 * Captures values within nested nested workflows
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class CapturedToDatabaseNested2Test extends AbstractDatabaseTestHelper {

	protected Map<String, Object> getWorkflowInputs() {
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("I", "abcd");
		return inputs;
	}
	
	protected Map<String, Object> getExpectedWorkflowInputs() {
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("I[]", "abcd");
		return inputs;
	}

	protected Map<String, Object> getExpectedWorkflowOutputs() {
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("O[]", "988");
		return inputs;
	}

	protected String getWorkflowName() {
		return "nested-2";
	}

	@Override
	protected Map<String, Object> getExpectedCollections() {
		return new HashMap<String, Object>();
	}
	
	@Override
	protected Map<String, Object> getExpectedIntermediateValues() {
		Map<String, Object> expectedIntermediateValues = new HashMap<String, Object>();

		String df0 = "ab03e390-005c-4e91-a284-6c0f451ee64d/";
		String df1 = "97e6c544-4acb-4627-aca8-fbcb714ebabc/";
		String df2 = "85dd9164-10ae-4d2a-8450-5ed905dfb7ac/";

		expectedIntermediateValues.put(df0 + "P1/i:X[]", "abcd");
		expectedIntermediateValues.put(df0 + "P1/o:Y1[]", "10");
		expectedIntermediateValues.put(df0 + "P1/o:Y2[]", "20");
		expectedIntermediateValues.put(df0 + "P4/i:X1[]", "968");
		expectedIntermediateValues.put(df0 + "P4/i:X2[]", "20");
		expectedIntermediateValues.put(df0 + "P4/o:Y[]", "988");
		expectedIntermediateValues.put(df0 + "PNested/i:I[]", "10");
		expectedIntermediateValues.put(df0 + "PNested/o:O[]", "968");
		expectedIntermediateValues.put(df1 + "P2/i:X[]", "10");
		expectedIntermediateValues.put(df1 + "P2/o:Y[]", "1000");
		expectedIntermediateValues.put(df1 + "P3/i:X[]", "1000");
		expectedIntermediateValues.put(df1 + "P3/o:Y[]", "968");
		expectedIntermediateValues.put(df1 + "PNested2/i:I[]", "968");
		expectedIntermediateValues.put(df1 + "PNested2/o:O[]", "968");
		expectedIntermediateValues.put(df2 + "P6/i:X[]", "968");
		expectedIntermediateValues.put(df2 + "P6/o:Y[]", "968");

		return expectedIntermediateValues;
	}

}
