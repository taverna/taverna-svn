package net.sf.taverna.t2.provenance.capture;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CapturedToDatabaseLineageExampleTest extends
		AbstractDatabaseTestHelper {

	@Override
	protected String getWorkflowName() {
		return "lineage-example";
	}

	@Override
	protected Map<String, Object> getExpectedWorkflowOutputs() {
		HashMap<String, Object> expected = new HashMap<String, Object>();
		expected.put("O1[0,0,0]", "a1b");
		expected.put("O1[0,0,1]", "d1");
		expected.put("O1[0,0,2]", "f");
		expected.put("O1[0,1,0]", "a2b");
		expected.put("O1[0,1,1]", "d1");
		expected.put("O1[0,1,2]", "f");
		expected.put("O1[1,0,0]", "a1b");
		expected.put("O1[1,0,1]", "d2");
		expected.put("O1[1,0,2]", "f");
		expected.put("O1[1,1,0]", "a2b");
		expected.put("O1[1,1,1]", "d2");
		expected.put("O1[1,1,2]", "f");
		expected.put("O2[0,0]", "a1b_d1_f");
		expected.put("O2[0,1]", "a2b_d1_f");
		expected.put("O2[1,0]", "a1b_d2_f");
		expected.put("O2[1,1]", "a2b_d2_f");
		return expected;
	}

	@Override
	protected Map<String, Object> getWorkflowInputs() {
		return new HashMap<String, Object>();
	}

	protected Map<String, Object> getExpectedIntermediateValues() {

		String dfId = dataflow.getInternalIdentier() + "/";
		
		Map<String, Object> expectedIntermediateValues = new HashMap<String, Object>();
		expectedIntermediateValues.put(dfId+"P0/Y[0]", "a1");
		expectedIntermediateValues.put(dfId+"P0/Y[1]", "a2");
		expectedIntermediateValues.put(dfId+"P1/X[0]", "a1");
		expectedIntermediateValues.put(dfId+"P1/X[1]", "a2");
		expectedIntermediateValues.put(dfId+"P1/Y[0]", "a1b");
		expectedIntermediateValues.put(dfId+"P1/Y[1]", "a2b");
		expectedIntermediateValues.put(dfId+"P2/Y[0]", "d1");
		expectedIntermediateValues.put(dfId+"P2/Y[1]", "d2");
		expectedIntermediateValues.put(dfId+"P3/Y[]", "f");
		expectedIntermediateValues.put(dfId+"P4/X1[0,0]", "a1b");
		expectedIntermediateValues.put(dfId+"P4/X1[0,1]", "a2b");
		expectedIntermediateValues.put(dfId+"P4/X1[1,0]", "a1b");
		expectedIntermediateValues.put(dfId+"P4/X1[1,1]", "a2b");
		expectedIntermediateValues.put(dfId+"P4/X2[0,0]", "d1");
		expectedIntermediateValues.put(dfId+"P4/X2[0,1]", "d1");
		expectedIntermediateValues.put(dfId+"P4/X2[1,0]", "d2");
		expectedIntermediateValues.put(dfId+"P4/X2[1,1]", "d2");
		expectedIntermediateValues.put(dfId+"P4/X3[0,0]", "f");
		expectedIntermediateValues.put(dfId+"P4/X3[0,1]", "f");
		expectedIntermediateValues.put(dfId+"P4/X3[1,0]", "f");
		expectedIntermediateValues.put(dfId+"P4/X3[1,1]", "f");
		expectedIntermediateValues.put(dfId+"P4/Y1[0,0,0]", "a1b");
		expectedIntermediateValues.put(dfId+"P4/Y1[0,0,1]", "d1");
		expectedIntermediateValues.put(dfId+"P4/Y1[0,0,2]", "f");
		expectedIntermediateValues.put(dfId+"P4/Y1[0,1,0]", "a2b");
		expectedIntermediateValues.put(dfId+"P4/Y1[0,1,1]", "d1");
		expectedIntermediateValues.put(dfId+"P4/Y1[0,1,2]", "f");
		expectedIntermediateValues.put(dfId+"P4/Y1[1,0,0]", "a1b");
		expectedIntermediateValues.put(dfId+"P4/Y1[1,0,1]", "d2");
		expectedIntermediateValues.put(dfId+"P4/Y1[1,0,2]", "f");
		expectedIntermediateValues.put(dfId+"P4/Y1[1,1,0]", "a2b");
		expectedIntermediateValues.put(dfId+"P4/Y1[1,1,1]", "d2");
		expectedIntermediateValues.put(dfId+"P4/Y1[1,1,2]", "f");
		expectedIntermediateValues.put(dfId+"P4/Y2[0,0]", "a1b_d1_f");
		expectedIntermediateValues.put(dfId+"P4/Y2[0,1]", "a2b_d1_f");
		expectedIntermediateValues.put(dfId+"P4/Y2[1,0]", "a1b_d2_f");
		expectedIntermediateValues.put(dfId +"P4/Y2[1,1]", "a2b_d2_f");

		return expectedIntermediateValues;

	}

}
