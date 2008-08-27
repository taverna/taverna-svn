package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author David Withers
 *
 */
public class ChangeDataflowInputPortDepthEditTest {

	private DataflowInputPortImpl dataflowInputPort;
	
	private int depth;
	
	private int granularDepth;
	
	@Before
	public void setUp() throws Exception {
		depth = 3;
		granularDepth = 1;
		dataflowInputPort = new DataflowInputPortImpl("port name", depth, granularDepth, null);
	}

	@Test
	public void testDoEditAction() throws EditException {
		int newDepth = depth = 2;
		ChangeDataflowInputPortDepthEdit edit = new ChangeDataflowInputPortDepthEdit(dataflowInputPort, newDepth);
		assertEquals(depth, dataflowInputPort.getDepth());
		assertEquals(granularDepth, dataflowInputPort.getGranularInputDepth());		
		edit.doEditAction(dataflowInputPort);
		assertEquals(newDepth, dataflowInputPort.getDepth());
		assertEquals(granularDepth, dataflowInputPort.getGranularInputDepth());
	}

	@Test
	public void testUndoEditAction() throws EditException {
		int newDepth = depth = 2;
		ChangeDataflowInputPortDepthEdit edit = new ChangeDataflowInputPortDepthEdit(dataflowInputPort, newDepth);
		assertEquals(depth, dataflowInputPort.getDepth());
		assertEquals(granularDepth, dataflowInputPort.getGranularInputDepth());		
		edit.doEditAction(dataflowInputPort);
		edit.undoEditAction(dataflowInputPort);
		assertEquals(depth, dataflowInputPort.getDepth());
		assertEquals(granularDepth, dataflowInputPort.getGranularInputDepth());
	}

	@Test
	public void testCreateDataflowInputPortEdit() {
		ChangeDataflowInputPortDepthEdit edit = new ChangeDataflowInputPortDepthEdit(dataflowInputPort, 0);
		assertEquals(dataflowInputPort, edit.getSubject());
	}

}
