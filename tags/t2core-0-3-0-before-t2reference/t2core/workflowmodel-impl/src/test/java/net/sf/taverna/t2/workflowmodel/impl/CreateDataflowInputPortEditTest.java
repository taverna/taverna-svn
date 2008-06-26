package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author David Withers
 *
 */
public class CreateDataflowInputPortEditTest {

	private DataflowImpl dataflow;
	
	private String portName;

	private int portDepth;
	
	private int portGranularDepth;

	@Before
	public void setUp() throws Exception {
		dataflow = new DataflowImpl();
		portName = "port name";
		portDepth = 3;
		portGranularDepth = 2;
	}

	@Test
	public void testDoEditAction() throws EditException {
		CreateDataflowInputPortEdit edit = new CreateDataflowInputPortEdit(dataflow, portName, portDepth, portGranularDepth);
		assertEquals(0, dataflow.getInputPorts().size());
		edit.doEditAction(dataflow);
		assertEquals(1, dataflow.getInputPorts().size());
		DataflowInputPort inputPort = dataflow.getInputPorts().get(0);
		assertSame(dataflow, inputPort.getDataflow());
		assertEquals(portName, inputPort.getName());
		assertEquals(portDepth, inputPort.getDepth());
		assertEquals(portGranularDepth, inputPort.getGranularInputDepth());
	}

	@Test
	public void testUndoEditAction() throws EditException {
		CreateDataflowInputPortEdit edit = new CreateDataflowInputPortEdit(dataflow, portName, portDepth, portGranularDepth);
		assertEquals(0, dataflow.getInputPorts().size());
		edit.doEditAction(dataflow);
		edit.undoEditAction(dataflow);
		assertEquals(0, dataflow.getInputPorts().size());
	}

	@Test
	public void testCreateDataflowInputPortEdit() {
		CreateDataflowInputPortEdit edit = new CreateDataflowInputPortEdit(dataflow, portName, portDepth, portGranularDepth);
		assertEquals(dataflow, edit.getSubject());
	}

}
