package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

import org.junit.Before;
import org.junit.Test;

/**
 * @author David Withers
 * 
 */
public class ConnectDatalinkEditTest {

	private DatalinkImpl datalink;
	private EventForwardingOutputPort source;
	private EventHandlingInputPort sink;

	@Before
	public void setUp() throws Exception {
		source = new BasicEventForwardingOutputPort("output", 0, 0);
		sink = new DataflowInputPortImpl("input", 0, 0, null);
		datalink = new DatalinkImpl(source, sink);
	}

	@Test
	public void testDoEditAction() throws EditException {
		ConnectDatalinkEdit edit = new ConnectDatalinkEdit(datalink);
		assertEquals(0, datalink.getSource().getOutgoingLinks().size());
		assertNull(datalink.getSink().getIncomingLink());
		edit.doEditAction(datalink);
		assertEquals(1, datalink.getSource().getOutgoingLinks().size());
		assertEquals(datalink, datalink.getSource().getOutgoingLinks()
				.iterator().next());
		assertEquals(datalink, datalink.getSink().getIncomingLink());
	}

	@Test
	public void testUndoEditAction() throws EditException {
		ConnectDatalinkEdit edit = new ConnectDatalinkEdit(datalink);
		assertEquals(0, datalink.getSource().getOutgoingLinks().size());
		assertNull(datalink.getSink().getIncomingLink());
		edit.doEditAction(datalink);
		edit.undoEditAction(datalink);
		assertEquals(0, datalink.getSource().getOutgoingLinks().size());
		assertNull(datalink.getSink().getIncomingLink());
	}

	@Test
	public void testConnectDatalinkEdit() {
		ConnectDatalinkEdit edit = new ConnectDatalinkEdit(datalink);
		assertEquals(datalink, edit.getSubject());
	}

}
