package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author David Withers
 *
 */
public class CreateDataflowOutputPortEditTest {

	private DataflowImpl dataflow;
	
	private String portName;

	@Before
	public void setUp() throws Exception {
		dataflow = new DataflowImpl();
		portName = "port name";
	}

	@Test
	public void testDoEditAction() throws EditException {
		CreateDataflowOutputPortEdit edit = new CreateDataflowOutputPortEdit(dataflow, portName);
		assertEquals(0, dataflow.getOutputPorts().size());
		edit.doEditAction(dataflow);
		assertEquals(1, dataflow.getOutputPorts().size());
		DataflowOutputPort outputPort = dataflow.getOutputPorts().get(0);
		assertSame(dataflow, outputPort.getDataflow());
		assertEquals(portName, outputPort.getName());
	}

	@Test
	public void testUndoEditAction() throws EditException {
		CreateDataflowOutputPortEdit edit = new CreateDataflowOutputPortEdit(dataflow, portName);
		assertEquals(0, dataflow.getOutputPorts().size());
		edit.doEditAction(dataflow);
		edit.undoEditAction(dataflow);
		assertEquals(0, dataflow.getOutputPorts().size());
	}

	@Test
	public void testCreateDataflowOutputPortEdit() {
		CreateDataflowOutputPortEdit edit = new CreateDataflowOutputPortEdit(dataflow, portName);
		assertEquals(dataflow, edit.getSubject());
	}

}
