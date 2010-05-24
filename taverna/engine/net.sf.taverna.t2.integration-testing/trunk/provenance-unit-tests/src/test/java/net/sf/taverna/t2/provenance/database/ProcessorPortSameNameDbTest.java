package net.sf.taverna.t2.provenance.database;

import java.sql.SQLException;
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
public class ProcessorPortSameNameDbTest extends AbstractDbTestHelper {

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

		String df0 = dataflow.getInternalIdentifier() + "/";

		expectedIntermediateValues.put(df0 + "input/o:value[]", "input");
		expectedIntermediateValues.put(df0 + "T2_1051/i:parameter[]", "input");
		expectedIntermediateValues.put(df0 + "T2_1051/o:parameter[]", "output");

		return expectedIntermediateValues;
	}

	@Override
	protected Map<String, Object> getExpectedCollections() {
		return new HashMap<String, Object>();
	}

	@Ignore("Disabled while doing T2-1308")
	@Test	
	@Override
	public void testPortBinding() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		super.testPortBinding();
	}
	
	@Override
	protected Set<String> getExpectedProcesses() {
		List<String> processes = Arrays.asList( 
				"input[]", "T2_1051[]");
		return new HashSet<String>(processes);
	}
	
	@Ignore("Disabled while doing T2-1308")
	@Test		
	@Override
	public void testPort() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		// TODO Auto-generated method stub
		super.testPort();
	}
	
	@Ignore("Disabled while doing T2-1308")
	@Test		
	@Override
	public void testProcessorEnactmentDataBindings() throws Exception {
		// TODO Auto-generated method stub
		super.testProcessorEnactmentDataBindings();
	}
}
