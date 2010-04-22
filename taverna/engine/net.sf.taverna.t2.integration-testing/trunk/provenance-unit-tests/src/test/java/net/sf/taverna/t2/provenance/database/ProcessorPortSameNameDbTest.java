package net.sf.taverna.t2.provenance.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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

	@Ignore("Disabled while doing T2-1308")
	@Test	
	@Override
	public void testVarBindings() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		super.testVarBindings();
	}
	
	@Ignore("Disabled while doing T2-1308")
	@Test		
	@Override
	public void testVars() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		// TODO Auto-generated method stub
		super.testVars();
	}
}
