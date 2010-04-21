package net.sf.taverna.t2.provenance.capture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.provenance.ProvenanceTestHelper;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.MergePort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;

import org.junit.Before;
import org.junit.Test;

/**
 * test workflow: nested-2.t2flow
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
	protected Map<String, Object> getExpectedIntermediateValues() {
		Map<String, Object> expectedIntermediateValues = new HashMap<String, Object>();

		String df0 = "ab03e390-005c-4e91-a284-6c0f451ee64d/";
		String df1 = "97e6c544-4acb-4627-aca8-fbcb714ebabc/";
		String df2 = "85dd9164-10ae-4d2a-8450-5ed905dfb7ac/";

		expectedIntermediateValues.put(df0 + "P1/X[]", "abcd");
		expectedIntermediateValues.put(df0 + "P1/Y1[]", "10");
		expectedIntermediateValues.put(df0 + "P1/Y2[]", "20");
		expectedIntermediateValues.put(df0 + "P4/X1[]", "968");
		expectedIntermediateValues.put(df0 + "P4/X2[]", "20");
		expectedIntermediateValues.put(df0 + "P4/Y[]", "988");
		expectedIntermediateValues.put(df0 + "PNested/I[]", "10");
		expectedIntermediateValues.put(df0 + "PNested/O[]", "968");
		expectedIntermediateValues.put(df1 + "P2/X[]", "10");
		expectedIntermediateValues.put(df1 + "P2/Y[]", "1000");
		expectedIntermediateValues.put(df1 + "P3/X[]", "1000");
		expectedIntermediateValues.put(df1 + "P3/Y[]", "968");
		expectedIntermediateValues.put(df1 + "PNested2/I[]", "968");
		expectedIntermediateValues.put(df1 + "PNested2/O[]", "968");
		expectedIntermediateValues.put(df2 + "P6/X[]", "968");
		expectedIntermediateValues.put(df2 + "P6/Y[]", "968");

		return expectedIntermediateValues;
	}

}
