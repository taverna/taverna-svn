package org.embl.ebi.escience.testhelpers;

import java.io.ByteArrayInputStream;


import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;


/*
 * Utility for building workflows for the puroposes of testing
 */

public class WorkflowFactory 
{
	/*
	 * returns a simple ScuflModel for a workflow containing just a string constant and an output.
	 */
	public static ScuflModel getSimpleWorkflowModel() throws Exception
	{
		return buildWorkflowModel(getSimpleWorkflowXML());					
	}
	
	/*
	 * builds a workflow from a given piece of xml
	 */
	protected static ScuflModel buildWorkflowModel(String xml) throws Exception
	{
		ScuflModel model=new ScuflModel();
		ByteArrayInputStream instr = new ByteArrayInputStream(xml.getBytes());
		XScuflParser.populate(instr,model,null);
		
		return model;
		
	}
	
	/*
	 * XML for a simple workflow containing just a string constant and an output
	 */
	private static String getSimpleWorkflowXML()
	{
		String result="<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
						"<s:scufl xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\" version=\"0.2\" log=\"0\">" +
						"<s:workflowdescription lsid=\"urn:lsid:www.mygrid.org.uk:operation:0Y60ZJP45I0\" author=\"tester\" title=\"simple workflow\" />" +
						"<s:processor name=\"String_Constant\" boring=\"false\">" +
						"<s:stringconstant>String Value</s:stringconstant>"+
						"</s:processor>"+
						"<s:link source=\"String_Constant:value\" sink=\"out\" />"+
						"<s:sink name=\"out\" />"+
						"</s:scufl>";	
		return result;
	}

}
