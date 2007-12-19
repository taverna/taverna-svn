package org.embl.ebi.escience.acceptancetests;

import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.testhelpers.acceptance.WorkflowTestCase;

public class ComplexTypeWorkflowTest extends WorkflowTestCase {
	
	public void testIteratingArrays() throws Exception
	{
		Map outputs = executeWorkflow("XMLSplitterArrays", new HashMap(), defaultWorkflowEventListener());
		assertEquals("wrong number of outputs",1,outputs.size());
		DataThing output = (DataThing) outputs.get("out");
		
		if (assertNotNull("output does not exist",output))
		{
			String xmlOutput = output.getDataObject().toString();
			assertEquals("generated xml is wrong","<WSArrayofData><data><type>type_one</type><content>content_one</content></data><data><type>type_two</type><content>content_two</content></data><data><type>type_three</type><content>content_three</content></data><data><type>type_four</type><content>content_four</content></data></WSArrayofData>",xmlOutput);
		}				
	}
}
