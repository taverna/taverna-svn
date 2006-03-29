package org.embl.ebi.escience.testhelpers;

import java.io.ByteArrayInputStream;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;

/**
 * Utility for building workflows for the puroposes of testing
 */

public class WorkflowFactory {
	/**
	 * returns a simple ScuflModel for a workflow containing just a string
	 * constant and an output.
	 */
	public static ScuflModel getSimpleWorkflowModel() throws Exception {
		return buildWorkflowModel(getSimpleWorkflowXML());
	}
	
	/**
	 * returns a simple ScuflModel for a workflow that contains an iteration over a list of 4 elements.
	 * 
	 * @throws Exception
	 */
	public static ScuflModel getSimpleIteratingWorkflow() throws Exception
	{
		return buildWorkflowModel(getSimpleIteratingWorkflowXML());
	}

	/**
	 * builds a workflow from a given piece of xml
	 */
	public static ScuflModel buildWorkflowModel(String xml) throws Exception {
		ScuflModel model = new ScuflModel();
		ByteArrayInputStream instr = new ByteArrayInputStream(xml.getBytes());
		XScuflParser.populate(instr, model, null);

		return model;

	}

	/**
	 * XML for a simple workflow containing just a string constant and an output
	 */
	private static String getSimpleWorkflowXML() {
		String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<s:scufl xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\" version=\"0.2\" log=\"0\">"
				+ "<s:workflowdescription lsid=\"urn:lsid:www.mygrid.org.uk:operation:0Y60ZJP45I0\" author=\"tester\" title=\"simple workflow\" />"
				+ "<s:processor name=\"String_Constant\" boring=\"false\">"
				+ "<s:stringconstant>String Value</s:stringconstant>" + "</s:processor>"
				+ "<s:link source=\"String_Constant:value\" sink=\"out\" />" + "<s:sink name=\"out\" />" + "</s:scufl>";
		return result;
	}

	private static String getSimpleIteratingWorkflowXML() {
		String result = "";
		result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		result += "<s:scufl xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\" version=\"0.2\" log=\"0\">";
		result += "<s:workflowdescription lsid=\"urn:lsid:www.mygrid.org.uk:operation:TEZZ8KNBY46\" author=\"\" title=\"\" />";
		result += "<s:processor name=\"String_Constant\" boring=\"true\">";
		result += "<s:stringconstant>a,b,c,d</s:stringconstant>";
		result += "</s:processor>";
		result += "<s:processor name=\"Beanshell_scripting_host\">";
		result += "<s:beanshell>";
		result += "<s:scriptvalue>String out = in;</s:scriptvalue>";
		result += "<s:beanshellinputlist>";
		result += "<s:beanshellinput s:syntactictype=\"'text/plain'\">in</s:beanshellinput>";
		result += "</s:beanshellinputlist>";
		result += "<s:beanshelloutputlist>";
		result += "<s:beanshelloutput s:syntactictype=\"'text/plain'\">out</s:beanshelloutput>";
		result += "</s:beanshelloutputlist>";
		result += "</s:beanshell>";
		result += "</s:processor>";
		result += "<s:processor name=\"regex\">";
		result += "<s:local>org.embl.ebi.escience.scuflworkers.java.SplitByRegex</s:local>";
		result += "</s:processor>";
		result += "<s:link source=\"String_Constant:value\" sink=\"regex:string\" />";
		result += "<s:link source=\"regex:split\" sink=\"Beanshell_scripting_host:in\" />";
		result += "<s:link source=\"Beanshell_scripting_host:out\" sink=\"out\" />";
		result += "<s:sink name=\"out\" />";
		result += "</s:scufl>";

		return result;
	}

}
