package net.sf.taverna.t2.provenance.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test a workflow in which a processor has an output port called the same as an input port.
 * Test for bug T2-1051
 * 
 * @author Stian Soiland-Reyes
 * 
 */
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

		String df0 = dataflow.getIdentifier() + "/";

		expectedIntermediateValues.put(df0 + "input/o:value[]", "input");
		expectedIntermediateValues.put(df0 + "T2_1051/i:parameter[]", "input");
		expectedIntermediateValues.put(df0 + "T2_1051/o:parameter[]", "output");

		return expectedIntermediateValues;
	}

	@Override
	protected Map<String, Object> getExpectedCollections() {
		return new HashMap<String, Object>();
	}
	
	@Override
	protected Set<String> getExpectedProcesses() {
		List<String> processes = Arrays.asList( 
				"input[]", "T2_1051[]");
		return new HashSet<String>(processes);
	}

	@Test
	@Ignore("Disabled while doing T2-1308")
	@Override
	public void fetchProcessorData() throws Exception {
		super.fetchProcessorData();
	}

}
