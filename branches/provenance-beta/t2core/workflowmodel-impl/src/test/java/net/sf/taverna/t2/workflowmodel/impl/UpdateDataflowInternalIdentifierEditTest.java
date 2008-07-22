package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;

import org.junit.Test;


public class UpdateDataflowInternalIdentifierEditTest {
	private static Edits edits = new EditsImpl();
	
	@Test
	public void testDoEdit() throws Exception {
		Dataflow df = edits.createDataflow();
		edits.getUpdateDataflowInternalIdentifierEdit(df, "123").doEdit();
		assertEquals("The internal id should be 123","123",df.getInternalIdentier());
	}
	
	@Test 
	public void testUndo() throws Exception {
		Dataflow df = edits.createDataflow();
		Edit<?> edit = edits.getUpdateDataflowInternalIdentifierEdit(df, "123");
		String oldID=df.getInternalIdentier();
		edit.doEdit();
		edit.undo();
		assertEquals("The id should be reset to its original value",oldID,df.getInternalIdentier());
	}
}
