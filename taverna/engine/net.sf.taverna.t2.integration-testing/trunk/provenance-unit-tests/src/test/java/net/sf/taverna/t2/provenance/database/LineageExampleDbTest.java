package net.sf.taverna.t2.provenance.database;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;

/**
 * Traces lineage over iterations.
 * Captures collections.
 * 
 * @author Stian Soiland-Reyes
 *
 */
public class LineageExampleDbTest extends
		AbstractDbTestHelper {

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
	protected Map<String, Object> getExpectedWorkflowPortCollections() {
		HashMap<String, Object> expected = new HashMap<String, Object>();
		
		expected.put("o:O1[]", JSONArray.toList(JSONArray.fromString(
				"[ [ ['a1b', 'd1', 'f'], ['a2b', 'd1', 'f']], [ ['a1b', 'd2', 'f'], ['a2b', 'd2', 'f'] ] ]" 
				)));
		expected.put("o:O2[]", JSONArray.toList(JSONArray.fromString(
				"[ ['a1b_d1_f', 'a2b_d1_f'], ['a1b_d2_f', 'a2b_d2_f'] ]" 
				)));
		
		return expected;
	}


	protected Map<String, Object> getExpectedWorkflowCollections() {
		String dfId = dataflow.getInternalIdentifier() + "/";
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(dfId + "P0/o:Y[]", Arrays.asList("a1", "a2"));
		expected.put(dfId + "P2/o:Y[]", Arrays.asList("d1", "d2"));
		expected.put(dfId + "P4/o:Y1[0,0]", Arrays.asList("a1b", "d1", "f"));
		expected.put(dfId + "P4/o:Y1[0,1]", Arrays.asList("a2b", "d1", "f"));
		expected.put(dfId + "P4/o:Y1[1,0]", Arrays.asList("a1b", "d2", "f"));
		expected.put(dfId + "P4/o:Y1[1,1]", Arrays.asList("a2b", "d2", "f"));		
		return expected;
	}
	

	protected Map<String, Object> getExpectedCollections() {
		String dfId = dataflow.getInternalIdentifier() + "/";
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(dfId + "P0/o:Y[]", Arrays.asList("a1", "a2"));
		expected.put(dfId + "P2/o:Y[]", Arrays.asList("d1", "d2"));
		expected.put(dfId + "P4/o:Y1[0,0]", Arrays.asList("a1b", "d1", "f"));
		expected.put(dfId + "P4/o:Y1[0,1]", Arrays.asList("a2b", "d1", "f"));
		expected.put(dfId + "P4/o:Y1[1,0]", Arrays.asList("a1b", "d2", "f"));
		expected.put(dfId + "P4/o:Y1[1,1]", Arrays.asList("a2b", "d2", "f"));		
		return expected;
	}
	
	@Override
	protected Map<String, Object> getWorkflowInputs() {
		return new HashMap<String, Object>();
	}

	protected Map<String, Object> getExpectedIntermediateValues() {

		String dfId = dataflow.getInternalIdentifier() + "/";
		
		Map<String, Object> expectedIntermediateValues = new HashMap<String, Object>();
		expectedIntermediateValues.put(dfId+"P0/o:Y[0]", "a1");
		expectedIntermediateValues.put(dfId+"P0/o:Y[1]", "a2");
		expectedIntermediateValues.put(dfId+"P1/i:X[0]", "a1");
		expectedIntermediateValues.put(dfId+"P1/i:X[1]", "a2");
		expectedIntermediateValues.put(dfId+"P1/o:Y[0]", "a1b");
		expectedIntermediateValues.put(dfId+"P1/o:Y[1]", "a2b");
		expectedIntermediateValues.put(dfId+"P2/o:Y[0]", "d1");
		expectedIntermediateValues.put(dfId+"P2/o:Y[1]", "d2");
		expectedIntermediateValues.put(dfId+"P3/o:Y[]", "f");
		expectedIntermediateValues.put(dfId+"P4/i:X1[0,0]", "a1b");
		expectedIntermediateValues.put(dfId+"P4/i:X1[0,1]", "a2b");
		expectedIntermediateValues.put(dfId+"P4/i:X1[1,0]", "a1b");
		expectedIntermediateValues.put(dfId+"P4/i:X1[1,1]", "a2b");
		expectedIntermediateValues.put(dfId+"P4/i:X2[0,0]", "d1");
		expectedIntermediateValues.put(dfId+"P4/i:X2[0,1]", "d1");
		expectedIntermediateValues.put(dfId+"P4/i:X2[1,0]", "d2");
		expectedIntermediateValues.put(dfId+"P4/i:X2[1,1]", "d2");
		expectedIntermediateValues.put(dfId+"P4/i:X3[0,0]", "f");
		expectedIntermediateValues.put(dfId+"P4/i:X3[0,1]", "f");
		expectedIntermediateValues.put(dfId+"P4/i:X3[1,0]", "f");
		expectedIntermediateValues.put(dfId+"P4/i:X3[1,1]", "f");
		expectedIntermediateValues.put(dfId+"P4/o:Y1[0,0,0]", "a1b");
		expectedIntermediateValues.put(dfId+"P4/o:Y1[0,0,1]", "d1");
		expectedIntermediateValues.put(dfId+"P4/o:Y1[0,0,2]", "f");
		expectedIntermediateValues.put(dfId+"P4/o:Y1[0,1,0]", "a2b");
		expectedIntermediateValues.put(dfId+"P4/o:Y1[0,1,1]", "d1");
		expectedIntermediateValues.put(dfId+"P4/o:Y1[0,1,2]", "f");
		expectedIntermediateValues.put(dfId+"P4/o:Y1[1,0,0]", "a1b");
		expectedIntermediateValues.put(dfId+"P4/o:Y1[1,0,1]", "d2");
		expectedIntermediateValues.put(dfId+"P4/o:Y1[1,0,2]", "f");
		expectedIntermediateValues.put(dfId+"P4/o:Y1[1,1,0]", "a2b");
		expectedIntermediateValues.put(dfId+"P4/o:Y1[1,1,1]", "d2");
		expectedIntermediateValues.put(dfId+"P4/o:Y1[1,1,2]", "f");
		expectedIntermediateValues.put(dfId+"P4/o:Y2[0,0]", "a1b_d1_f");
		expectedIntermediateValues.put(dfId+"P4/o:Y2[0,1]", "a2b_d1_f");
		expectedIntermediateValues.put(dfId+"P4/o:Y2[1,0]", "a1b_d2_f");
		expectedIntermediateValues.put(dfId +"P4/o:Y2[1,1]", "a2b_d2_f");

		return expectedIntermediateValues;

	}

	@Override
	protected Set<String> getExpectedProcesses() {
		List<String> processes = Arrays.asList("P0[]", 
				"P2[]", "P1[0]", "P1[1]",
				 "P3[]", 
				 "P4[0,0]", "P4[0,1]", 
				 "P4[1,0]", 
				 "P4[1,1]");
		return new HashSet<String>(processes);
	}

}
