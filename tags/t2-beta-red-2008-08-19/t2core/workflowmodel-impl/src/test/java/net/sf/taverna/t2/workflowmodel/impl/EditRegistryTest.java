package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;

import org.junit.Test;
import static org.junit.Assert.*;

public class EditRegistryTest {

	@Test
	public void testOneAndOnlyOne() throws Exception {
		assertEquals("There should be 1 instance",1,EditsRegistry.getInstance().getInstances().size());
	}
	@Test
	public void testImplFound() {
		Edits edits = EditsRegistry.getEdits();
		assertTrue("The edit should be an instance of EditsImpl",edits instanceof EditsImpl);
	}
}
